package me.laravieira.willy.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.kernel.Kernel;
import me.laravieira.willy.player.DiscordPlayer;

public class Discord {

	private static DiscordClient client = DiscordClient.create(Config.getDiscordToken());
	private static GatewayDiscordClient gateway;
	private static boolean ready = false;

	public static void login() {
		gateway = client.login().block();
		DiscordPlayer.load();
		gateway.on(ReadyEvent.class).subscribe(event -> Discord.setReady(true), error -> errorDisplay(error));
		gateway.on(DisconnectEvent.class).subscribe(event -> Discord.setReady(false), error -> errorDisplay(error));
		gateway.on(MemberUpdateEvent.class).subscribe(event -> Kernel.onMemberUpdate(event), error -> errorDisplay(error));
		gateway.on(VoiceStateUpdateEvent.class).subscribe(event -> DiscordPlayer.onVoiceChannelUpdate(event), error-> errorDisplay(error));
		gateway.on(MessageCreateEvent.class).subscribe(event -> DiscordMessage.onMessage(event.getMessage()), error -> errorDisplay(error));
    	MyLogger.getLogger().info("Discord instance has benn openned.");
    	MyLogger.loadDiscordLogger();
	}

	private static void errorDisplay(Object error) {
		MyLogger.getLogger().severe(""+error);
	}

	public static void logout() {
		if(ready) {
			gateway.logout().block();
			gateway.onDisconnect().block();
		}
	}
	
	public static boolean isConnected() {
		return ready;
	}
	
    public static DiscordClient getBot() {
    	return client;
    }
    
    public static GatewayDiscordClient getBotGateway() {
    	return gateway;
    }
    
    private static void setReady(boolean ready) {
    	Discord.ready = ready;
    }
}
