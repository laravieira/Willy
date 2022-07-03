package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.logging.Logger;

public class CommandStatus implements CommandListener {
    public static final String COMMAND = "status";

    public void execute(@NotNull Logger console, int count, String[] args) {
        console.info("-------------------------- Status ----------------------------");
        console.info("Discord: "+(new Discord().isConnected()?"Connected":"Disconnected"));
        console.info("Contexts: "+ ContextStorage.size()+" in use");
        console.info("Bitly Use: "+ Config.getBoolean("bitly.enable"));
        console.info("Music Players: "+ DiscordPlayer.getPlayers().size()+" loaded");
        long time  = (new Date().getTime()- Willy.getWilly().getStartTime())/1000;
        console.info("UpTime: "+time/(3600*24)+"d, "+(time%(3600*24))/(3600)+"h, "+(time%(3600)/60)+"m, "+time%60+"s.");
        console.info("---------------------------------------------------------------");
    }
}
