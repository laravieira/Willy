package me.laravieira.willy.chat.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.IntentSet;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.logging.LogRecord;

public class Discord implements WillyChat {

	private static DiscordClient client;
	private static GatewayDiscordClient gateway;
	private static boolean ready = false;

	@Override
	public void connect() {
		if(!Config.has("discord.enable") || !Config.getBoolean("discord.enable")) {
			Willy.getLogger().warning("Discord instance disabled.");
			return;
		}
		if(!Config.has("discord.token")) {
			Willy.getLogger().severe("Discord token not found.");
			return;
		}

		EventDispatcher eventDispatcher = EventDispatcher.builder().build();
		eventDispatcher.on(ChatInputInteractionEvent.class).subscribe(DiscordListener::onCommand, Discord::errorDisplay);
		eventDispatcher.on(ConnectEvent.class)             .subscribe(_ -> Discord.setReady(true), Discord::errorDisplay);
		eventDispatcher.on(ReconnectEvent.class)           .subscribe(_ -> Discord.setReady(true), Discord::errorDisplay);
		eventDispatcher.on(ReadyEvent.class)               .subscribe(_ -> Discord.setReady(true), Discord::errorDisplay);
		eventDispatcher.on(DisconnectEvent.class)          .subscribe(_ -> Discord.setReady(false), Discord::errorDisplay);
		eventDispatcher.on(SessionInvalidatedEvent.class)  .subscribe(_ -> Discord.setReady(false), Discord::errorDisplay);
		eventDispatcher.on(MemberUpdateEvent.class)        .subscribe(DiscordListener::onMemberUpdate, Discord::errorDisplay);
		eventDispatcher.on(MessageCreateEvent.class)       .subscribe(event -> DiscordListener.onMessage(event.getMessage()), Discord::errorDisplay);

		client = DiscordClient.create(Config.getString("discord.token"));
		gateway = client
			.gateway()
			.setEventDispatcher(eventDispatcher)
			.setEnabledIntents(IntentSet.all())
			.login()
			.block();
		Willy.getLogger().registerDiscordHandler();
		registerCommands();
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

	public static void sendLog(LogRecord record) {
		if(gateway == null || !Config.has("discord.admin.log"))
			return;
		String message = "```yaml\r\n" + "[" + record.getLevel() + "] " + record.getMessage() + "```";
		gateway.getChannelById(Snowflake.of(Config.getLong("discord.admin.log")))
			.doOnSuccess(channel -> ((MessageChannel)channel).createMessage(message).subscribe())
			.block();
	}

	private static void registerCommands() {
		gateway.getRestClient()
			.getApplicationId()
			.doOnSuccess(id -> Command.commandsList()
				.forEach(command -> gateway.getRestClient()
					.getApplicationService()
					.createGuildApplicationCommand(id, Config.getLong("discord.admin.guild"), command.register())
					.doOnSuccess(ignore -> Willy.getLogger().info("Command "+command.getName()+" registered."))
					.doOnError(error -> Willy.getLogger().warning("Command "+command.getName()+" failed with: "+error.getMessage()))
					.subscribe()
				))
			.block();
	}
}
