package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.feature.youtube.Youtube;
import org.jetbrains.annotations.NotNull;

public class CommandYoutube implements CommandListener {
    public static final String COMMAND = "youtube";
    public static final String DESCRIPTION = "Get Youtube direct download link.";
    public static final String OPTION = "url";

    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND)
                .description(DESCRIPTION)
                .addOption(ApplicationCommandOptionData.builder()
                        .name(OPTION)
                        .description("The URL of the Youtube video.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).ifPresent(value -> {
            String url = value.asString();
            if (!url.matches("<\\\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>")) {
                event.reply("This is not a valid URL.").subscribe();
                return;
            }

            Youtube ytd = new Youtube(url);
            if(!ytd.getVideo()) {
                event.reply("Unable to get video link.").subscribe();
            }
            event.reply(ytd.getDownloadLink()).subscribe();
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
