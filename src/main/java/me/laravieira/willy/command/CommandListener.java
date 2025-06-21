package me.laravieira.willy.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;

public interface CommandListener {
    ApplicationCommandRequest register();

    InteractionApplicationCommandCallbackReplyMono execute(@NotNull ChatInputInteractionEvent event);

    String getName();
    String getDescription();
}
