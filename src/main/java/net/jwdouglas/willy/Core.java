package net.jwdouglas.willy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.watson.assistant.v2.model.MessageResponse;

import discord4j.core.object.entity.Message;

public class Core {

	private static Map<Date, Message> toClear = new HashMap<Date, Message>();
	
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
				message.getChannel().block().createMessage(text).block();
				
				Logger log = MyLogger.getConsoleLogger();
				log.info("Message transaction in a private chat.");
				//log.info("Received: "+content);
				//log.info("Returned: "+text);
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
		
		toClear.put(Date.from(message.getTimestamp()), message);
		
		// Message transaction with Watson
		MessageResponse msgr =  Watson.sendMessage(content);
		if(msgr.getOutput().getGeneric() != null && !msgr.getOutput().getGeneric().isEmpty()) {
			String type = msgr.getOutput().getGeneric().get(0).getResponseType();
			String text = msgr.getOutput().getGeneric().get(0).getText();
			if(type.equalsIgnoreCase("text")) {
				Message response = message.getChannel().block().createMessage(text).block();
				toClear.put(Date.from(response.getTimestamp()), response);
				
				Logger log = MyLogger.getConsoleLogger();
				log.info("Message transaction in a public chat.");
				//log.info("Received: "+content);
				//log.info("Returned: "+text);
			}
		}
	}
	
	public static void clearChannel() {
		if(Config.getClearChats()) {
			Date date = new Date(new Date().getTime()-Config.getClearTime());
			Map<Date, Message> toRemove = new HashMap<Date, Message>();
			toClear.forEach((key, value) -> {
				if(key.before(date)) {
					value.delete("Time expired.").block();
					toRemove.put(key, value);
				}
			});
			toRemove.forEach((key, value) -> {
				toClear.remove(key, value);
			});
			
			Logger log = MyLogger.getConsoleLogger();
			log.info("Message deleted from a public chat.");
		}
	}
}
