package me.laravieira.willy.chat.discord;

import discord4j.common.ReactorResources;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;
import lombok.Setter;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.WillyChat;
import me.laravieira.willy.utils.PassedInterval;
import reactor.core.scheduler.Schedulers;

import java.util.Date;

public class Discord implements WillyChat {
    private static GuildMessageChannel heartbeatChannel;
    private static PassedInterval heartbeatRefresh;
	private static GatewayDiscordClient gateway;
    @Setter
	private static boolean ready = false;

	@Override
	public void connect() {
		if(!Config.getBoolean("discord.enable")) {
			Willy.getLogger().warning("Discord service is disabled.");
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
		eventDispatcher.on(MessageCreateEvent.class)       .subscribe(event -> DiscordListener.onMessage(event.getMessage()), Discord::errorDisplay);

        // Sample to resolve connection reset issue, check https://github.com/Discord4J/Discord4J/issues/1020
        String token = Config.getString("discord.token");
        ReactorResources resources = DiscordClient.create(token).getCoreResources().getReactorResources();
		gateway = DiscordClient.builder(token)
            .setReactorResources(ReactorResources.builder()
                .httpClient(resources.getHttpClient().keepAlive(false))
                .blockingTaskScheduler(resources.getBlockingTaskScheduler())
                .timerTaskScheduler(resources.getTimerTaskScheduler())
                .build()
            )
            .build()
            .gateway()
			.setEventDispatcher(eventDispatcher)
			.setEnabledIntents(IntentSet.all())
            .login()
            .block();
        if (gateway == null) return;

		if(Config.has("discord.admin.log")) {
            gateway.getChannelById(Snowflake.of(Config.getLong("discord.admin.log")))
                .publishOn(Schedulers.boundedElastic())
                .doOnError(error -> Willy.getLogger().fine(error.getMessage()))
                .doOnSuccess(channel -> {
                    Willy.getLogger().registerDiscordHandler(((GuildMessageChannel) channel));
                    Willy.getLogger().fine("Discord admin log channel found.");
                }).block();
		}else Willy.getLogger().warning("Discord admin log channel disabled.");

		if(Config.has("discord.admin.heartbeat") && Config.has("discord.admin.heartbeat_interval")) {
            gateway.getChannelById(Snowflake.of(Config.getLong("discord.admin.heartbeat")))
                .publishOn(Schedulers.boundedElastic())
                .doOnError(error -> Willy.getLogger().fine(error.getMessage()))
                .doOnSuccess(channel -> {
                    heartbeatChannel = ((GuildMessageChannel)channel);
                    heartbeatRefresh = new PassedInterval(Config.getLong("discord.admin.heartbeat_interval"));
                    heartbeatRefresh.start();
			        Willy.getLogger().fine("Discord heartbeat channel found.");
                }).block();
		}else Willy.getLogger().warning("Discord heartbeat channel disabled.");

		registerCommands();
    	Willy.getLogger().fine("Discord service connected successfully.");
	}

	public static void onReady() {
		Willy.getLogger().fine("Discord service ready.");
		Discord.setReady(true);
	}

	@Override
	public void disconnect() {
		if(ready) {
			gateway.logout().subscribe();
		}
	}

	@Override
	public boolean isConnected() {
		return ready;
	}

	@Override
	public void refresh() {
        if(ready && heartbeatChannel != null && heartbeatRefresh.hasPassedInterval()) {
            heartbeatRefresh.reset();
            getBotGateway().updatePresence(ClientPresence.online(ClientActivity.custom("Living in the clouds"))).block();
            heartbeatChannel.createMessage(new Date().toString()).block();
        }
	}

    @Override
    public String getName() {
        return "Discord";
    }

    private static void errorDisplay(Throwable error) {
		Willy.getLogger().severe("Discord: "+error.getMessage());
	}

    public static GatewayDiscordClient getBotGateway() {
    	return gateway;
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
					.doOnSuccess(ignore -> Willy.getLogger().fine("Discord global command "+command.getName()+" registered."))
					.doOnError(error -> Willy.getLogger().warning("Discord global command "+command.getName()+" failed with: "+error.getMessage()))
					.subscribe()
				);
				Command.adminCommandsList().forEach(command -> gateway.getRestClient()
					.getApplicationService()
					.createGuildApplicationCommand(id, Config.getLong("discord.admin.guild"), command.register())
					.doOnSuccess(ignore -> Willy.getLogger().fine("Discord admin command "+command.getName()+" registered."))
					.doOnError(error -> Willy.getLogger().warning("Discord admin command "+command.getName()+" failed with: "+error.getMessage()))
					.subscribe()
				);
			})
			.subscribe();
	}
}
