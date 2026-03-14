package me.laravieira.willy.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Context;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.command.CommandChannel;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandOpenAi implements CommandListener {
    public static final String COMMAND = "openai";
    public static final String DESCRIPTION = "Talk to OpenAI directly.";
    public static final String OPTION = "message";

    @Override
    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .addOption(ApplicationCommandOptionData.builder()
                .name(OPTION)
                .description("The message to OpenAI")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build()
            ).build();
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono execute(@NotNull ChatInputInteractionEvent event) {
        if(event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).isPresent()) {
            String value = event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
            UUID id = UUID.nameUUIDFromBytes("willy-console".getBytes());
            Context context = Context.of(id, new CommandChannel(event), "Console", "Console");

            WillyMessage message = new WillyMessage(value);
            message.setText(value);
            context.process(message);
        }
        return null;
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
