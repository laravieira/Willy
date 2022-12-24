package me.laravieira.willy.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;

public interface CommandListener {
    ApplicationCommandRequest register();

    void execute(@NotNull ChatInputInteractionEvent event);

    String getName();
    String getDescription();
}
