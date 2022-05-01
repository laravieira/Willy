package me.laravieira.willy.chat.discord;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.kernel.Context;
import me.laravieira.willy.kernel.Kernel;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.internal.WillyUtils;

public class DiscordListener {
	
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
			Willy.getLogger().severe(e.getMessage());
			for(StackTraceElement elem : e.getStackTrace())
				Willy.getLogger().severe(elem.getMethodName()+" ("+elem.getLineNumber()+")");
		}
	}
	
	public static void onPrivateTextChannelMessage(MessageChannel channel, User user, Message message) {
		String content = parseWillyId(message.getContent());
		String id = "discord-"+user.getId().asString();
		if(startWith(content)) return;

		content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
		
		DiscordContext context = DiscordContext.getContext(channel, message, user, id);
		context.setDeleteMessages(false);
		context.getWatsonMessager().sendTextMessage(content);
		Willy.getLogger().info("Message transaction in a private chat.");
	}
	
	public static void onPublicTextChannelMessage(MessageChannel channel, User user, Message message) {
		String content = parseWillyId(message.getContent());
		String id = "discord-"+user.getId().asString();
		
		if(content.startsWith("!help")) {
			content = content.substring(5);
			floodChat(channel);
		}else if(startWith(content)) return;
		if(content.isEmpty()) return;
		
		if(!WillyUtils.hasWillyCall(content) && !Context.getContexts().containsKey(id)) return;

		content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
		
		DiscordContext context = DiscordContext.getContext(channel, message, user, id);
		if(!Kernel.checkForPlayQuestion(context, content))
			context.getWatsonMessager().sendTextMessage(content);
		Willy.getLogger().info("Message transaction in a public chat.");
	}
	
	private static String parseWillyId(String message) {
		Long id = Config.getLong("discord.client_id");
		String name = Config.getString("willy-name");
		return message.replaceAll("<@"+ id +">", name);
	}
	
	private static boolean startWith(String message) {
		for(String prefix : (List<String>)Config.getList("ignore_if_start_with"))
			if(message.startsWith(prefix))
				return true;
		return false;
	}

	public static void refresh() {
		if(Config.getBoolean("discord.clear_public_chats")) {
			Long now = new Date().getTime();
			Map<Long, Message> toRemove = new HashMap<Long, Message>();
			messages.forEach((key, value) -> {
				if(key < now) {
					try {
						value.delete("Time expired.").block();
						Willy.getLogger().info("Message deleted from a public chat.");
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
	
	public static void autoDeleteMessage(long timestamp, Message message) {
		messages.put(timestamp, message);
	}
	
	private static void floodChat(MessageChannel channel) {
		long clearTime = Config.getLong("discord.clear_after_wait");
		try {Message response = null;
			
			response = channel.createMessage(specs -> { specs.addEmbed(embed -> {
				embed.setImage("https://github.com/laravieira/Willy/raw/master/assets/help/helps.png");
			});}).block(); messages.put(new Date().getTime()+clearTime, response);
			
			response = channel.createMessage(specs -> { specs.addEmbed(
				embed -> {embed.setImage("https://github.com/laravieira/Willy/raw/master/assets/help/help.png");
			});}).block(); messages.put(new Date().getTime()+clearTime, response);

			response = channel.createMessage(specs -> { specs.addEmbed(embed -> {
				embed.setImage("https://github.com/laravieira/Willy/raw/master/assets/help/be-help.png");
			});}).block(); messages.put(new Date().getTime()+clearTime, response);

			response = channel.createMessage(specs -> {	specs.addEmbed(embed -> {
				embed.setThumbnail("https://github.com/laravieira/Willy/raw/master/assets/help/piscadela.jpg");
			});}).block(); messages.put(new Date().getTime()+clearTime, response);
			
			Willy.getLogger().info("Chat flood sent to "+channel.getId().asString()+".");
			
		}catch(RuntimeException e) {
			Willy.getLogger().warning("Flood failure by an exception: "+e.getMessage());
		}
	}
}
