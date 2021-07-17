package me.laravieira.willy.discord;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.kernel.Context;
import me.laravieira.willy.kernel.Kernel;
import discord4j.core.object.entity.channel.MessageChannel;

public class DiscordMessage {
	
	private static Map<Long, Message> messages = new HashMap<Long, Message>();

	public static void onMessage(Message message) {
		try {
			if(message.getAuthor().isPresent() && message.getContent() instanceof String) {
				MessageChannel channel = message.getChannel().block();
				User author = message.getAuthor().get();
				if(author.isBot()) return;
				
				if(channel.getType().equals(Type.DM) || channel.getType().equals(Type.GROUP_DM))
					onPrivateTextChannelMessage(channel, author, message);
				else
					onPublicTextChannelMessage(channel, author, message);
			}
		}catch(Exception e) {
			MyLogger.getLogger().severe(e.getMessage());
			for(StackTraceElement elem : e.getStackTrace())
				MyLogger.getLogger().severe(elem.getMethodName()+" ("+elem.getLineNumber()+")");
		}
	}
	
	public static void onPrivateTextChannelMessage(MessageChannel channel, User user, Message message) {
		String content = parseWillyId(message.getContent());
		String id = "discord-"+user.getId().asString();
		if(startWith(content)) return;

		content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
		
		Context context = Context.getDiscordContext(channel, message, user, id);
		context.setDeleteMessages(false);
		context.getWatsonMessager().sendTextMessage(content);
		MyLogger.getLogger().info("Message transaction in a private chat.");
	}
	
	public static void onPublicTextChannelMessage(MessageChannel channel, User user, Message message) {
		String content = parseWillyId(message.getContent());
		String id = "discord-"+user.getId().asString();
		
		if(content.startsWith("!help")) {
			content = content.substring(5);
			floodChat(channel);
		}else if(startWith(content)) return;
		if(content.isEmpty()) return;
		
		if(!hasWillyCall(content) && !Context.getContexts().containsKey(id)) return;

		content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
		
		Context context = Context.getDiscordContext(channel, message, user, id);
		if(!Kernel.checkForPlayQuestion(context, content))
			context.getWatsonMessager().sendTextMessage(content);
		MyLogger.getLogger().info("Message transaction in a public chat.");
	}
	
	private static String parseWillyId(String message) {
		return message.replaceAll("<@"+Config.getDiscordID()+">", Config.getName());
	}
	
	private static boolean startWith(String message) {
		for(String prefix : Config.getIgnoreStartWith())
			if(message.startsWith(prefix))
				return true;
		return false;
	}
	
	private static boolean hasWillyCall(String message) {
		if(message.contains(Config.getName()))
			return true;
		for(String alias : Config.getAliases())
			if(message.contains(alias))
				return true;
		return false;
	}

	public static void refresh() {
		if(Config.getClearChats()) {
			Long now = new Date().getTime();
			Map<Long, Message> toRemove = new HashMap<Long, Message>();
			messages.forEach((key, value) -> {
				if(key < now) {
					try {
						value.delete("Time expired.").block();
						MyLogger.getLogger().info("Message deleted from a public chat.");
					}catch(RuntimeException e) {}
					toRemove.put(key, value);
				}
			});
			toRemove.forEach((key, value) -> {
				messages.remove(key, value);
			});
			
		}
	}

	public static Map<Long, Message> getMessages() {
		return messages;
	}
	
	public static void addMessage(long timestamp, Message message) {
		messages.put(timestamp, message);
	}
	
	private static void floodChat(MessageChannel channel) {
		try {Message response = null;
			
			response = channel.createMessage(specs -> { specs.setEmbed(embed -> {
				embed.setImage("https://jwdouglas.net/willy_files/helps.png");
			});}).block(); messages.put(new Date().getTime()+Config.getClearTime(), response);
			
			response = channel.createMessage(specs -> { specs.setEmbed(
				embed -> {embed.setImage("https://jwdouglas.net/willy_files/help.png");
			});}).block(); messages.put(new Date().getTime()+Config.getClearTime(), response);

			response = channel.createMessage(specs -> { specs.setEmbed(embed -> {
				embed.setImage("https://jwdouglas.net/willy_files/be-help.png");
			});}).block(); messages.put(new Date().getTime()+Config.getClearTime(), response);

			response = channel.createMessage(specs -> {	specs.setEmbed(embed -> {
				embed.setThumbnail("https://jwdouglas.net/willy_files/piscadela.jpg");
			});}).block(); messages.put(new Date().getTime()+Config.getClearTime(), response);
			
			MyLogger.getLogger().info("Chat flood send to "+channel.getId().asString()+".");
			
		}catch(RuntimeException e) {
			MyLogger.getLogger().warning("Flood failure by an exception: "+e.getMessage());
		}
	}
}
