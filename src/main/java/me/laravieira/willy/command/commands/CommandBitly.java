package me.laravieira.willy.command.commands;

import com.opsmatters.bitly.Bitly;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
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
    public InteractionApplicationCommandCallbackReplyMono execute(@NotNull ChatInputInteractionEvent event) {
        if(event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).isPresent()) {
            String link = event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
            try {
                if(!Config.getBoolean("bitly.enable")) {
                    throw new Exception("Bitly is not enabled.");
                }
                if(!Config.has("bitly.token")) {
                    throw new Exception("Bitly token was not found.");
                }

                Thread thread = new Thread(() -> {
                    try {
                        Bitly bitly = new Bitly(Config.getString("bitly.token"));
                        if(!bitly.bitlinks().shorten(link).isPresent()) {
                            throw new Exception("Bitly failed to shorten the link.");
                        }

                        String shortLink = bitly.bitlinks().shorten(link).get().getLink();
                        if (shortLink == null || shortLink.isEmpty()) {
                            Willy.getLogger().fine(STR."command bitly \{link} failed.");
                            event.createFollowup("Something didn't work right.").subscribe();
                            return;
                        }
                        if (shortLink.equals(link)) {
                            Willy.getLogger().fine(STR."command bitly \{link} returned the same link.");
                            event.createFollowup("Maybe it's already short enough.").subscribe();
                            return;
                        }

                        Willy.getLogger().fine(STR."command bitly \{link} returned \{shortLink}.");
                        event.createFollowup(shortLink).subscribe();
                    } catch (Exception e) {
                        Willy.getLogger().warning(e.getMessage());
                        event.createFollowup(e.getMessage()).subscribe();
                    }
                });
                thread.setDaemon(true);
                thread.start();

                return event.reply("Shortening the link, please wait...");
            } catch (Exception e) {
                Willy.getLogger().warning(e.getMessage());
                return event.reply(e.getMessage());
            }
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
