package me.laravieira.willy.web;

import java.security.cert.CertificateException;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import me.laravieira.willy.Willy;
import javax.net.ssl.SSLException;

public class Server {
	private static Logger         log     = Willy.getLogger();
	private static Channel        channel = null;
	private static EventLoopGroup boss    = new NioEventLoopGroup();
	private static EventLoopGroup worker  = new NioEventLoopGroup();

	public static ApplicationProtocolNegotiationHandler getHTTP2ServerAPNHandler() {
		return new ApplicationProtocolNegotiationHandler(
				ApplicationProtocolNames.HTTP_2
		) {
			@Override
			protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
				if(!ApplicationProtocolNames.HTTP_2.equals(protocol))
					return;
				ctx.pipeline().addLast(
						Http2FrameCodecBuilder.forServer().build(),
						new HTTP2Handler());}
		};
	}

	public static ApplicationProtocolNegotiationHandler getHTTPServerAPNHandler() {
		return new ApplicationProtocolNegotiationHandler(
				ApplicationProtocolNames.HTTP_1_1
		) {
			@Override
			protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
				if(!ApplicationProtocolNames.HTTP_1_1.equals(protocol))
					return;
				ctx.pipeline().addLast(new HTTPHandler());
			}
		};
	}

	public static void load() {
		if(!Willy.getConfig().asBoolean("web.enable"))
			return;
		try {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			SslContext context = SslContextBuilder
					.forServer(ssc.certificate(), ssc.privateKey())
					.sslProvider(SslProvider.JDK)
					.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
					.applicationProtocolConfig(new ApplicationProtocolConfig(
							ApplicationProtocolConfig.Protocol.ALPN,
							ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
							ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
							ApplicationProtocolNames.HTTP_1_1,
							ApplicationProtocolNames.HTTP_2))
					.build();

			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							if(context == null)
								return;
							ch.pipeline().addLast(
									context.newHandler(ch.alloc()),
									Server.getHTTPServerAPNHandler(),
									Server.getHTTP2ServerAPNHandler());
						}
					});
			int port = Willy.getConfig().asInt("web.port");
			channel = bootstrap.bind(port).sync().channel();
			log.info("Server opened on port "+port+".");

			worker.shutdownGracefully();
			boss.shutdownGracefully();
		}catch(CertificateException | SSLException | InterruptedException e) {
			log.severe(e.getMessage());
			Willy.getWilly().stop();
		}
	}

	public static void close() {
		if(channel != null) {
			channel.disconnect();
			channel.deregister();
			channel.close();
		}
	}

	public static boolean isRunning() {
		return channel.isOpen();
	}
}
