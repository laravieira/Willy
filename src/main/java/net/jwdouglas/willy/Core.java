package net.jwdouglas.willy;

import java.util.logging.Logger;

import com.ibm.watson.assistant.v2.model.MessageResponse;

import discord4j.core.object.entity.Message;

public class Core {

	private static Logger log = MyLogger.getLogger();
	
	public static void onPrivateMessage(Message message) {
		// Convert tags of Willy to his name
		String content = message.getContent().get().replaceAll("<@"+Config.getDiscordID()+">", Config.getName());
		
		// Check if the message is to Willy (no name checks)
		String[] cantStartWith = {"!", "?", "@", "/", "\\", "//"};
		for(int i = 0; i < cantStartWith.length; i++) {
			if(content.startsWith(cantStartWith[i])) {return;}
		}
		
		// Message transaction with Watson
		MessageResponse msgr =  Watson.sendMessage(content);
		if(msgr.getOutput().getGeneric() != null && !msgr.getOutput().getGeneric().isEmpty()) {
			String type = msgr.getOutput().getGeneric().get(0).getResponseType();
			String text = msgr.getOutput().getGeneric().get(0).getText();
			if(type.equalsIgnoreCase("text")) {
				log.info("Received: "+content);
				log.info("Returned: "+text);
				message.getChannel().block().createMessage(text).block();
			}
		}
	}
	
	public static void onTextChannelMessage(Message message) {
		// Convert tags of Willy to his name
		String content = message.getContent().get().replaceAll("<@"+Config.getDiscordID()+">", Config.getName());
		
		// Check if the message is to Willy (check name)
		String[] cantStartWith = {"!", "?", "@", "/", "\\", "//"};
		for(int i = 0; i < cantStartWith.length; i++) {
			if(content.startsWith(cantStartWith[i])) {return;}
		}boolean haveAlias = false;
		if(content.contains(Config.getName())) {haveAlias = true;}
		for(int i = 0; i < Config.getAliases().size(); i++) {
			if(content.contains(Config.getAliases().get(i))) {
				haveAlias = true; break;}
		}if(!haveAlias) {return;}
		
		// Message transaction with Watson
		MessageResponse msgr =  Watson.sendMessage(content);
		if(msgr.getOutput().getGeneric() != null && !msgr.getOutput().getGeneric().isEmpty()) {
			String type = msgr.getOutput().getGeneric().get(0).getResponseType();
			String text = msgr.getOutput().getGeneric().get(0).getText();
			if(type.equalsIgnoreCase("text")) {
				log.info("Received: "+content);
				log.info("Returned: "+text);
				message.getChannel().block().createMessage(text).block();
			}
		}
	}
}
