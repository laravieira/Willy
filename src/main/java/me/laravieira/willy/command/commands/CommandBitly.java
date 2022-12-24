package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.feature.bitly.Bitly;
import org.jetbrains.annotations.NotNull;

public class CommandBitly implements CommandListener {
    public static final String COMMAND = "bitly";
    public static final String DESCRIPTION = "Use bitly shorten api.";
    public static final String OPTION = "url";

    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .addOption(ApplicationCommandOptionData.builder()
                .name(OPTION)
                .description("The URL to be shorten by bitly.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build()
            ).build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        if(!Bitly.canUse) {
            event.reply("Bitly is not enabled.").subscribe();
            return;
        }
        event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).ifPresent(value -> {
            String url = value.asString();
            if (!url.matches("<\\\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>")) {
                event.reply("This is not a valid URL.").subscribe();
                return;
            }

            String shortLink = new Bitly(url).getShort();
            if (shortLink == null || shortLink.isEmpty()) {
                event.reply("Something didn't work right.").subscribe();
                return;
            }
            if (shortLink.equals(url)) {
                event.reply("Maybe it's already short enough.").subscribe();
                return;
            }
            event.reply(shortLink).subscribe();
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
