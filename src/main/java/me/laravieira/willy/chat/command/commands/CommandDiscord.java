package me.laravieira.willy.chat.command.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.discord.Discord;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandDiscord implements CommandListener {
    public static final String COMMAND = "user";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count > 1) {
            Snowflake userId = Snowflake.of(args[1]);
            console.info("User: -----------------------");
            Object raw = Discord.getBotGateway().getUserById(userId).block();
            if(raw instanceof User user) {
                console.info("uAvatar: " + user.getAvatarUrl());
                console.info("uDefaultAvatar: " + user.getDefaultAvatarUrl());
                console.info("uDiscriminator: " + user.getDiscriminator());
                console.info("uMention: " + user.getMention());
                console.info("uTag: " + user.getTag());
                console.info("uUsername: " + user.getUsername());
                console.info("uID: " + user.getId().asString());
            }
        }else {
            console.info("This command need an user id, type 'help' to see usage.");
        }
    }
}
