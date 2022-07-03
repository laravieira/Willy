package me.laravieira.willy.chat.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.command.commands.*;
import me.laravieira.willy.internal.WillyChat;

public class Command implements WillyChat {
	private static Thread line = null;

	public void onCommand(String command, String[] args) {
		Thread thread = new Thread(() -> {
			switch (command) {
				case CommandStatus.COMMAND -> new CommandStatus().execute(Willy.getLogger(), args.length, args);
				case CommandWatson.COMMAND -> new CommandWatson().execute(Willy.getLogger(), args.length, args);
				case CommandYoutube.COMMAND -> new CommandYoutube().execute(Willy.getLogger(), args.length, args);
				case CommandBitly.COMMAND -> new CommandBitly().execute(Willy.getLogger(), args.length, args);
				case CommandContext.COMMAND -> new CommandContext().execute(Willy.getLogger(), args.length, args);
				case CommandDiscordPlayer.COMMAND -> new CommandDiscordPlayer().execute(Willy.getLogger(), args.length, args);
				case CommandDiscordNoADM.COMMAND -> new CommandDiscordNoADM().execute(Willy.getLogger(), args.length, args);
				case CommandDiscord.COMMAND -> new CommandDiscord().execute(Willy.getLogger(), args.length, args);
				case CommandWhatsapp.COMMAND -> new CommandWhatsapp().execute(Willy.getLogger(), args.length, args);
				case CommandOpenAi.COMMAND -> new CommandOpenAi().execute(Willy.getLogger(), args.length, args);
				case CommandHelp.COMMAND -> new CommandHelp().execute(Willy.getLogger(), args.length, args);
				case "stop" -> Willy.getWilly().stop();
				default -> unknow();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public static void unknow() {
		Willy.getLogger().info("Unknow command, to get help type 'help'.");
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
