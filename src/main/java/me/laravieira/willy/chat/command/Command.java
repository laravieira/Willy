package me.laravieira.willy.chat.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;

public class Command implements WillyChat {
	private static Thread line = null;

	public void onCommand(String command, String[] args) {
		Thread thread = new Thread(() -> {
			switch (command) {
				case "stop" -> Willy.getWilly().stop();
				case "status" -> CommandListener.status();
				case "talk" -> CommandListener.talk(args);
				case "youtube" -> CommandListener.youtube(args);
				case "short" -> CommandListener.shortLink(args);
				case "context" -> CommandListener.context(args);
				case "player" -> CommandListener.player(args);
				case "noadm" -> CommandListener.noadm(args);
				case "user" -> CommandListener.user(args);
				case "whats" -> CommandListener.whatsapp(args);
				case "openai" -> CommandListener.openai(args);
				case "help" -> CommandListener.help();
				default -> CommandListener.unknow();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void connect() {
		try{
			// Will break if there is no console input available
			if(line == null) {
				line = new Thread(() -> {
					try {
						BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
						while(!Willy.getWilly().getStop()) {
							String[] commandLine = input.readLine().split(" ");
							if(commandLine.length > 0) {
								String com = commandLine[0].toLowerCase();

								// Remove prefix of first command
								if(com.startsWith("/") || com.startsWith("\\") || com.startsWith("!") || com.startsWith("'")
								|| com.startsWith("?") || com.startsWith("#")  || com.startsWith("@") || com.startsWith("\""))
									com = com.substring(1);

								// Remove suffix of first command
								if(com.endsWith("\"") || com.endsWith("'"))
									com = com.substring(0, com.length()-1);

								onCommand(com, commandLine);
							}
						}
					} catch (Exception e) {
						Willy.getLogger().warning("Command Line is not available here, sorry.");
					}
				});
				line.setDaemon(true);
				line.start();
			}
		}catch (Exception ignored) {}
	}

	@Override
	public void disconnect() {
		if(isConnected())
			line.interrupt();
		line = null;
	}

	@Override
	public boolean isConnected() {
		return line != null && line.isAlive();
	}

	@Override
	public void refresh() {
	}
}
