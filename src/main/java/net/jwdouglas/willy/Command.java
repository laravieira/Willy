package net.jwdouglas.willy;

import java.util.logging.Logger;

public class Command {
	
	public static void onCommand(String[] command) {
		
		// Remove prefix, if exits
		if(command[0].startsWith("/"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("\\")) command[0] = command[0].substring(1);
		if(command[0].startsWith("!"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("?"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("#"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("@"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("'"))  command[0] = command[0].substring(1);
		if(command[0].startsWith("\"")) command[0] = command[0].substring(1);
		
		// Remove suffix, if exits
		if(command[0].endsWith("\""))   command[0] = command[0].substring(0, command[0].length()-1);
		if(command[0].endsWith("'"))    command[0] = command[0].substring(0, command[0].length()-1);
		
		// Search for commands
		if(command[0].equalsIgnoreCase("stop")) {
			Willy.stop();
		}else if(command[0].equalsIgnoreCase("talk")) {
			if(command.length > 1) {
				String message = "";
				for(int i = 1; i < command.length; i++)
					message += " "+command[i];
				Watson.logMessage(Watson.sendMessage(message.substring(1)), false);
			}else {
				MyLogger.getLogger().info("You need to type a message after 'talk' command.");
			}
		}else if(command[0].equalsIgnoreCase("help")) {
			Logger log = MyLogger.getLogger();
			log.info("---------------------------------------------------------------");
			log.info("---                        Help Page                        ---");
			log.info("---------------------------------------------------------------");
			log.info("stop : Close all connections and stop Willy.");
			log.info("talk : Send a message to Willy, full response will be printed.");
			log.info("help : Show all know commands and their descriptions.");
			log.info(" ");
			log.info("---------------------------------------------------------------");
		}else {
			MyLogger.getLogger().info("Unkwon command, to get help type 'help'.");;
		}
	}
}
