package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.feature.bitly.Bitly;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.storage.ContextStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class CommandStatus implements CommandListener {
    public static final String COMMAND = "status";
    public static final String DESCRIPTION = "Willy's status";

    @Override
    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        StringBuilder list = new StringBuilder();
        list.append("**Willy status** `").append(new Date()).append("`").append("\r\n");
        list.append("```yaml").append("\r\n");

        list.append("discord: ").append(new Discord().isConnected() ? "Connected" : "Disconnected").append("\r\n");
        list.append("openai: ").append(new OpenAi().isConnected() ? "Connected" : "Disconnected").append("\r\n");
        list.append("whatsapp: ").append(new Whatsapp().isConnected() ? "Connected" : "Disconnected").append("\r\n");
        list.append("telegram: ").append(new Telegram().isConnected() ? "Connected" : "Disconnected").append("\r\n");
        list.append("contexts: ").append(ContextStorage.size()).append(" in use").append("\r\n");
        list.append("bitly: ").append(Bitly.canUse).append("\r\n");
        list.append("players: ").append(DiscordPlayer.getPlayers().size()).append(" loaded").append("\r\n");
        long time  = (new Date().getTime()- Willy.getWilly().getStartTime())/1000;
        list.append("up-time: ").append(time / (3600 * 24)).append("d, ").append((time % (3600 * 24)) / (3600)).append("h, ").append(time % (3600) / 60).append("m, ").append(time % 60).append("s.").append("\r\n");

        list.append("```");
        event.reply(list.toString()).subscribe();
    }

    @Override
    public String getName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
