package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.discord.DiscordNoADM;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandDiscordNoADM implements CommandListener {
    public static final String COMMAND = "noadm";

    @SuppressWarnings("StatementWithEmptyBody")
    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count > 1) {
            if(count > 3 && args[1].equalsIgnoreCase("ban")) {
                String reason = "";
                for(int i = 4; i < count; reason += args[i++]);
                DiscordNoADM.ban(args[2], args[3], reason);
            }else if(args[1].equalsIgnoreCase("unban")) {
                String reason = "";
                for(int i = 4; i < count; reason += args[i++]);
                DiscordNoADM.unban(args[2], args[3], reason);
            }else if(args[1].equalsIgnoreCase("op")) {
                DiscordNoADM.op(args[2], args[3]);
            }else if(args[1].equalsIgnoreCase("deop")) {
                DiscordNoADM.deop(args[2], args[3]);
            }else if(args[1].equalsIgnoreCase("mperms")) {
                DiscordNoADM.listMemberPermissions(args[2], args[3]);
            }else if(args[1].equalsIgnoreCase("rperms")) {
                DiscordNoADM.listRolePermissions(args[2], args[3]);
            }else if(args[1].equalsIgnoreCase("cmperms")) {
                DiscordNoADM.listChannelMemberPermissions(args[2], args[3], args[4]);
            }else if(args[1].equalsIgnoreCase("crperms")) {
                DiscordNoADM.listChannelRolePermissions(args[2], args[3], args[4]);
            }else {
                console.info("use: noadm [cmd] [guild_id] [member_id] reason");
            }
        }else {
            console.info("This command need sub command, type 'help' to see usage.");
        }
    }
}
