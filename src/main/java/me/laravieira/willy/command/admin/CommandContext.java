package me.laravieira.willy.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CommandContext implements CommandListener {
    public static final String COMMAND = "contexts";
    public static final String DESCRIPTION = "Print existing contexts list";
    public static final String OPTION = "clear";

    @Override
    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND)
                .description(DESCRIPTION)
                .addOption(ApplicationCommandOptionData.builder()
                    .name(OPTION)
                    .description(DESCRIPTION)
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .addChoice(ApplicationCommandOptionChoiceData.builder()
                        .name(OPTION)
                        .value(OPTION)
                        .build())
                    .required(false)
                    .build())
                .build();
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono execute(@NotNull ChatInputInteractionEvent event) {
        if(event.getOption(OPTION).isPresent()) {
            List<UUID> contexts = new ArrayList<>(Context.all().keySet());
            contexts.forEach(Context::destroy);
            return event.reply("All contexts were vanished.");
        }else {
            StringBuilder list = new StringBuilder();
            list.append("**Contexts alive** `").append(new Date()).append("`").append("\r\n");
            list.append("```yaml").append("\r\n");
            Context.all().forEach(
                (identifier, context) -> list.append(identifier)
                    .append(": ")
                    .append(context.getExpire().remaining() / 1000)
                    .append("s.")
                    .append("\r\n")
            );
            list.append("```");
            return event.reply(list.toString());
        }
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
