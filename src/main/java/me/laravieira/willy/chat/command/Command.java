package me.laravieira.willy.chat.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;

public class Command implements WillyChat {
	private Logger console = Willy.getLogger().getConsole();
	private static Thread line = null;
	private boolean connected = false;

	public void onCommand(String command, String[] args) {
		Thread thread = new Thread(() -> {
			if(command.equals("stop"))           {
                Willy.getWilly().stop();
			}else if(command.equals("status"))   {CommandListener.status(args);
			}else if(command.equals("talk"))     {CommandListener.talk(args);
			}else if(command.equals("youtube"))  {CommandListener.youtube(args);
			}else if(command.equals("short"))    {CommandListener.shortLink(args);
			}else if(command.equals("contexts")) {CommandListener.contexts(args);
			}else if(command.equals("player"))   {CommandListener.player(args);
			}else if(command.equals("noadm"))    {CommandListener.noadm(args);
			}else if(command.equals("user"))     {CommandListener.user(args);
			}else if(command.equals("whats"))    {CommandListener.whatsapp(args);
			}else if(command.equals("openai"))   {CommandListener.openai(args);
			}else if(command.equals("help"))     {CommandListener.help(args);
			}else {CommandListener.unknow();}
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
