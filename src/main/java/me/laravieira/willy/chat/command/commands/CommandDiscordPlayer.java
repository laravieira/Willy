package me.laravieira.willy.chat.command.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.VoiceChannel;
import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.internal.Config;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandDiscordPlayer implements CommandListener {
    public static final String COMMAND = "player";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count > 1) {
            VoiceChannel channel = (VoiceChannel) Discord.getBotGateway().getChannelById(Snowflake.of(Config.getString("ap.command_default_channel_id"))).block();
            if(channel == null)
                return;
            DiscordPlayer player = DiscordPlayer.createDiscordPlayer(channel);
            if(count > 2 && args[1].equalsIgnoreCase("add")) {
                player.add(args[2]);
            }else if(args[1].equalsIgnoreCase("play")) {
                player.play();
            }else if(args[1].equalsIgnoreCase("resume")) {
                player.resume();
            }else if(args[1].equalsIgnoreCase("pause")) {
                player.pause();
            }else if(args[1].equalsIgnoreCase("stop")) {
                player.stop();
            }else if(args[1].equalsIgnoreCase("next")) {
                player.next();
            }else if(args[1].equalsIgnoreCase("clear")) {
                player.clear();
            }else if(args[1].equalsIgnoreCase("destroy")) {
                player.destroy();
            }else if(args[1].equalsIgnoreCase("info")) {
                console.info("-------------------- Default Player Info ----------------------");
                console.info("Player status: "+(player.getPlayer().getPlayingTrack() == null?"EMPTY":player.getPlayer().getPlayingTrack().getState().name()));
                console.info("Playing track: "+(player.getPlayer().getPlayingTrack() == null?"EMPTY":player.getPlayer().getPlayingTrack().getInfo().title));
                console.info("Next track: "+(player.getTrackScheduler().getNext() == null?"EMPTY":player.getTrackScheduler().getNext().getInfo().title));
                console.info("Queue size: "+player.getTrackScheduler().getQueue().size());
                console.info("Channel status: "+(DiscordPlayer.isMemberConnectedTo(player.getChannel(), Snowflake.of(Config.getString("discord.client_id")))?"CONNECTED":"DISCONNECTED"));
                console.info("Channel identifier: "+player.getChannel().getName()+" ("+player.getChannel().getId().asString()+")");
                console.info("---------------------------------------------------------------");
            }else {
                console.info("Undefined command, type 'help' to see usage.");
            }
        }else {
            console.info("This command need sub command, type 'help' to see usage.");
        }
    }
}
