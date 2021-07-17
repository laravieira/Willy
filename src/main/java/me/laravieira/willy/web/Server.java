package me.laravieira.willy.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.laravieira.willy.Willy;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;

public class Server {
	private static Logger       log    = MyLogger.getLogger();
	private static ServerSocket server = null;
	private static boolean      listen = false;
	private static List<Thread> cons   = new ArrayList<Thread>();

	public static void load() {
		if(Config.getWebUse()) {
			try {
				if(server != null)
					server.close();
				server = new ServerSocket(Config.getWebPort());
				if(server.isClosed()) {
					server = null;
				}
			}catch(IOException e) {
				log.severe(e.getMessage());
				Willy.stop();
			}
			log.info("Web Server is working on port "+server.getLocalPort()+".");
		}
	}
	
	public static boolean isWorking() {
		if(server == null || server.isClosed())
			return false;
		else return true;
	}
	
	public static int getPort() {
		return server.getLocalPort();
	}
	
	public static int howConnections() {
		return cons.size();
	}
	
	public static void refresh() {
		if(server != null && !server.isClosed() && !listen) {
			listen = true;
			Thread thread = new Thread(() -> {
				try {
					Socket socket = server.accept();
					listen = false;
					new WebResquests(socket);

				}catch(IOException e) {}
			});
			thread.setDaemon(true);
			thread.start();
			
			cons.add(thread);
			for(int i = 0; i < cons.size(); i++)
				if(!cons.get(i).isAlive()) {
					cons.get(i).interrupt();
					cons.remove(i);
				}
		}else if(!listen && Config.getWebUse()) {
			load();
			if(!isWorking()) {
				log.severe("HTTP Server can't be restarted.");
				Willy.stop();
			}
		}
	}

	public static void close() {
		try {
			cons.forEach((connection) -> {
				connection.interrupt();
			});
			cons.clear();
			if(server != null)
				server.close();
			listen = false;
			server = null;
		} catch (IOException e) {
			log.severe(e.getMessage());
			Willy.stop();
			listen = false;
			server = null;
		}
	}
}
