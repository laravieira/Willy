package me.laravieira.willy.command.commands;

import com.opsmatters.bitly.Bitly;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.internal.Config;
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
        event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).ifPresent(value -> {
            String link = value.asString();
            try {
                if(!Config.getBoolean("bitly.enable")) {
                    throw new Exception("Bitly is not enabled.");
                }
                if(!Config.has("bitly.token")) {
                    throw new Exception("Bitly token was not found.");
                }
                Bitly bitly = new Bitly(Config.getString("bitly.token"));
                if(!bitly.bitlinks().shorten(link).isPresent()) {
                    throw new Exception("Bitly failed to shorten the link.");
                }

                String shortLink = bitly.bitlinks().shorten(link).get().getLink();
                if (shortLink == null || shortLink.isEmpty()) {
                    Willy.getLogger().fine(STR."command bitly \{link} failed.");
                    event.reply("Something didn't work right.").subscribe();
                    return;
                }
                if (shortLink.equals(link)) {
                    Willy.getLogger().fine(STR."command bitly \{link} returned the same link.");
                    event.reply("Maybe it's already short enough.").subscribe();
                    return;
                }

                Willy.getLogger().fine(STR."command bitly \{link} returned \{shortLink}.");
                event.reply(shortLink).subscribe();
            } catch (Exception e) {
                Willy.getLogger().warning(e.getMessage());
                event.reply(e.getMessage()).subscribe();
            }
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
