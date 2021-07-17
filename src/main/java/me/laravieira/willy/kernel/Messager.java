package me.laravieira.willy.kernel;

import java.util.function.Consumer;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.discord.Discord;

public class Messager {
	private Context context = null;
	
	public Messager(String id) {
		context = Context.getContext(id);
	}
	
	public Messager(Context context) {
		this.context = context;
	}
	
	public void sendMessage(String message) {
		if(context.getDiscordChannel() != null)
			sendDiscordTextMessage(message);
		if(context.getLogger() != null)
			sendConsoleMessage(message);
	}
	
	public void sendDiscordEmbedMessage(Consumer<EmbedCreateSpec> message) {
		try {
			Message msg = context.getDiscordChannel().createEmbed(message).block();
			context.saveDiscordMessage(msg);
		}catch (NullPointerException e) {
			Discord.login();
			Message msg = context.getDiscordChannel().createEmbed(message).block();
			context.saveDiscordMessage(msg);
		}
	}
	
	public void sendDiscordMessage(Consumer<MessageCreateSpec> message) {
		try {
			Message msg = context.getDiscordChannel().createMessage(message).block();
			context.saveDiscordMessage(msg);
		}catch (NullPointerException e) {
			Discord.login();
			Message msg = context.getDiscordChannel().createMessage(message).block();
			context.saveDiscordMessage(msg);
		}
	}
	
	public void sendDiscordTextMessage(String message) {
		try {
			Message msg = context.getDiscordChannel().createMessage(message).block();
			context.saveDiscordMessage(msg);
		}catch (NullPointerException e) {
			Discord.login();
			Message msg = context.getDiscordChannel().createMessage(message).block();
			context.saveDiscordMessage(msg);
		}
	}
	
	public void sendConsoleMessage(String message) {
		if(context.getDebugWatsonMessage())
			context.getWatsonMessager().debug(context.getLogger());
		else context.getLogger().info(message);
	}
}
