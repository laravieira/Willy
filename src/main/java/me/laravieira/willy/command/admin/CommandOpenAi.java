package me.laravieira.willy.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.chat.openai.OpenAiSender;
import me.laravieira.willy.command.CommandSender;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
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
            UUID context = UUID.nameUUIDFromBytes("willy-console".getBytes());
            ContextStorage.of(context).setUserSender(new CommandSender(event));

            Message message = new Message(context);
            message.setExpire(PassedInterval.DISABLE);
            message.setContent(value);
            message.setText(value);
            message.setFrom("Console");
            message.setTo(Willy.getWilly().getName());
            MessageStorage.add(message);

            new OpenAiSender(message.getContext()).sendText(message.getText());
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
