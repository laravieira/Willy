package me.laravieira.willy.chat.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ActivityData;
import discord4j.discordjson.json.gateway.PresenceUpdate;
import discord4j.gateway.intent.IntentSet;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;
import me.laravieira.willy.kernel.Kernel;
import me.laravieira.willy.feature.player.DiscordPlayer;
import reactor.core.publisher.Mono;

public class Discord implements WillyChat {

	private static DiscordClient client = DiscordClient.create(Willy.getConfig().asString("discord.token"));
	private static GatewayDiscordClient gateway;
	private static boolean ready = false;

	@Override
	public void connect() {
		if(!Willy.getConfig().asBoolean("discord.enable"))
			return;
		gateway = client
				.gateway()
				.setEnabledIntents(IntentSet.all())
				.login()
				.block();
		DiscordPlayer.load();
		gateway.on(ReadyEvent.class).subscribe(event -> Discord.setReady(true), error -> errorDisplay(error));
		gateway.on(DisconnectEvent.class).subscribe(event -> Discord.setReady(false), error -> errorDisplay(error));
		gateway.on(MemberUpdateEvent.class).subscribe(event -> Kernel.onMemberUpdate(event), error -> errorDisplay(error));
		gateway.on(VoiceStateUpdateEvent.class).subscribe(event -> DiscordPlayer.onVoiceChannelUpdate(event), error-> errorDisplay(error));
		gateway.on(MessageCreateEvent.class).subscribe(event -> DiscordListener.onMessage(event.getMessage()), error -> errorDisplay(error));
    	Willy.getLogger().info("Discord instance connected.");
	}

	@Override
	public void disconnect() {
		if(ready) {
			gateway.logout().block();
			gateway.onDisconnect().block();
		}
	}

	@Override
	public boolean isConnected() {
		return ready;
	}

	@Override
	public void refresh() {

	}

	private static void errorDisplay(Object error) {
		Willy.getLogger().severe(""+error);
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
