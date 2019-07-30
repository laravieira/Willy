package net.jwdouglas.willy;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Channel.Type;
import discord4j.core.object.entity.Message;

public class Discord {

	private static DiscordClient client     = new DiscordClientBuilder(Config.getDiscordToken()).build();

	public static void login() {
    	Thread ambient = new Thread(() -> {
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> onMessage(event.getMessage()));
	    	client.login().block();
    	});
    	ambient.setDaemon(true);
    	ambient.start();
    	while(!client.isConnected());
	}

	public static void logout() {
		if(client.isConnected())
			client.logout().block();
    	while(client.isConnected());
	}
	
	public static void onMessage(Message message) {
		// Check if have a message
		if(message.getAuthor().isPresent() && message.getContent().isPresent()) {
			
			// Check if writer is a bot
			if(message.getAuthor().get().isBot()) {return;}
			
			// Check what chat it
			Channel channel = message.getChannel().block();
			if(channel.getType().equals(Type.DM)) {
				Core.onPrivateMessage(message);
			}else if(channel.getType().equals(Type.GROUP_DM)) {
				Core.onPrivateMessage(message);
			}else if(channel.getType().equals(Type.GUILD_CATEGORY)) {
				Core.onTextChannelMessage(message);
			}else if(channel.getType().equals(Type.GUILD_NEWS)) {
				Core.onTextChannelMessage(message);
			}else if(channel.getType().equals(Type.GUILD_STORE)) {
				Core.onTextChannelMessage(message);
			}else if(channel.getType().equals(Type.GUILD_TEXT)) {
				Core.onTextChannelMessage(message);
			}else if(channel.getType().equals(Type.GUILD_VOICE)) {
				Core.onTextChannelMessage(message);
			}
		}
	}
	
	// Return Discord Client
    public static DiscordClient getBot() {return client;}
}
