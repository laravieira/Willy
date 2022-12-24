package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.bloom.BloomSender;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.command.CommandSender;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandBloom implements CommandListener {
    public static final String COMMAND = "bloom";
    public static final String DESCRIPTION = "Send message to Bloom AI.";
    public static final String OPTION = "message";

    @Override
    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .addOption(ApplicationCommandOptionData.builder()
                .name(OPTION)
                .description("The message to Bloom AI")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build()
            ).build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).ifPresent(value -> {
            UUID context = UUID.nameUUIDFromBytes("willy-console".getBytes());
            ContextStorage.of(context).setSender(new CommandSender(event));

            Message message = new Message(context);
            message.setExpire(PassedInterval.DISABLE);
            message.setContent(value.asString());
            message.setText(value.asString());
            message.setFrom("Console");
            message.setTo(Willy.getWilly().getName());
            MessageStorage.add(message);

            new BloomSender(message.getContext()).sendText(message.getText());
        });
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
