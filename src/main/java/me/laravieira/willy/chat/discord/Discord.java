package me.laravieira.willy.chat.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.intent.IntentSet;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

public class Discord implements WillyChat {
	private static GatewayDiscordClient gateway;
	private static boolean ready = false;

	@Override
	public void connect() {
		if(!Config.getBoolean("discord.enable")) {
			Willy.getLogger().warning("Discord instance disabled.");
			return;
		}
		if(!Config.has("discord.token")) {
			Willy.getLogger().severe("Discord token not found.");
			return;
		}
		if(!Config.has("discord.client_id")) {
			Willy.getLogger().severe("Discord client id not found.");
			return;
		}

		EventDispatcher eventDispatcher = EventDispatcher.builder().build();
		eventDispatcher.on(ChatInputInteractionEvent.class).subscribe(DiscordListener::onCommand, Discord::errorDisplay);
		eventDispatcher.on(ConnectEvent.class)             .subscribe(_ -> Discord.onReady(), Discord::errorDisplay);
		eventDispatcher.on(ReconnectEvent.class)           .subscribe(_ -> Discord.onReady(), Discord::errorDisplay);
		eventDispatcher.on(DisconnectEvent.class)          .subscribe(_ -> Discord.setReady(false), Discord::errorDisplay);
		eventDispatcher.on(SessionInvalidatedEvent.class)  .subscribe(_ -> Discord.setReady(false), Discord::errorDisplay);
		eventDispatcher.on(MemberUpdateEvent.class)        .subscribe(DiscordListener::onMemberUpdate, Discord::errorDisplay);
		eventDispatcher.on(MessageCreateEvent.class)       .subscribe(event -> DiscordListener.onMessage(event.getMessage()), Discord::errorDisplay);

		gateway = DiscordClient.create(Config.getString("discord.token"))
			.gateway()
			.setEventDispatcher(eventDispatcher)
			.setEnabledIntents(IntentSet.all())
			.login()
			.block();

		if(Config.has("discord.admin.log")) {
			Willy.getLogger().registerDiscordHandler();
			Willy.getLogger().info("Discord admin log channel initiated.");
		}else {
			Willy.getLogger().warning("Discord admin log channel not found.");
		}

		registerCommands();
    	Willy.getLogger().info("Discord instance loaded.");
	}

	public static void onReady() {
		Willy.getLogger().info("Discord instance ready.");
		Discord.setReady(true);
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

	private static void errorDisplay(Throwable error) {
		Willy.getLogger().severe(STR."Discord: \{error.getMessage()}");
	}

    public static GatewayDiscordClient getBotGateway() {
    	return gateway;
    }
    
    private static void setReady(boolean ready) {
    	Discord.ready = ready;
    }

	private static void registerCommands() {
		if(gateway == null)
			return;
		if(!Config.has("discord.admin.guild")) {
			Willy.getLogger().warning("Discord admin guild (server) not found.");
			return;
		}
		gateway.getRestClient()
			.getApplicationId()
			.doOnSuccess(id -> {
				Command.globalCommandsList().forEach(command -> gateway.getRestClient()
					.getApplicationService()
					.createGlobalApplicationCommand(id, command.register())
					.doOnSuccess(ignore -> Willy.getLogger().fine(STR."Discord global command \{command.getName()} registered."))
					.doOnError(error -> Willy.getLogger().warning(STR."Discord global command \{command.getName()} failed with: \{error.getMessage()}"))
					.subscribe()
				);
				Command.adminCommandsList().forEach(command -> gateway.getRestClient()
					.getApplicationService()
					.createGuildApplicationCommand(id, Config.getLong("discord.admin.guild"), command.register())
					.doOnSuccess(ignore -> Willy.getLogger().fine(STR."Discord admin command \{command.getName()} registered."))
					.doOnError(error -> Willy.getLogger().warning(STR."Discord admin command \{command.getName()} failed with: \{error.getMessage()}"))
					.subscribe()
				);
			})
			.block();
	}
}
