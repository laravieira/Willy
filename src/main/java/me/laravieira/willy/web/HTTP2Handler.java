package me.laravieira.willy.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;

public class HTTP2Handler extends ChannelDuplexHandler {
    private static final ByteBuf STATIC_RESPONSE = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(!(msg instanceof Http2HeadersFrame)) {
            super.channelRead(ctx, msg);
            return;
        }
        Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
        if(!headersFrame.isEndStream())
            return;

        ByteBuf content = ctx.alloc().buffer();
        content.writeBytes(STATIC_RESPONSE.duplicate());

        Http2Headers headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText());

        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(headersFrame.stream()));
        ctx.write(new DefaultHttp2DataFrame(content, true).stream(headersFrame.stream()));
    }
}
