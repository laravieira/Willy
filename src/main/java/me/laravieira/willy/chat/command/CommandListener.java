package me.laravieira.willy.chat.command;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import it.auties.whatsapp.model.contact.ContactJid;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.discord.DiscordNoADM;
import me.laravieira.willy.chat.openai.OpenAiSender;
import me.laravieira.willy.chat.watson.WatsonSender;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import me.laravieira.willy.context.Context;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.feature.bitly.Bitly;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.feature.youtube.Youtube;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class CommandListener {
    private static final Logger console = Willy.getLogger();

    static void status() {
        console.info("-------------------------- Status ----------------------------");
        console.info("Discord: "+(new Discord().isConnected()?"Connected":"Disconnected"));
        console.info("Contexts: "+ ContextStorage.size()+" in use");
        console.info("Bitly Use: "+ Config.getBoolean("bitly.enable"));
        console.info("Music Players: "+ DiscordPlayer.getPlayers().size()+" loaded");
        long time  = (new Date().getTime()- Willy.getWilly().getStartTime())/1000;
        console.info("UpTime: "+time/(3600*24)+"d, "+(time%(3600*24))/(3600)+"h, "+(time%(3600)/60)+"m, "+time%60+"s.");
        console.info("---------------------------------------------------------------");
    }

    static void youtube(@NotNull String[] args) {
        Youtube ytd;
        if(args.length == 2 && args[1].startsWith("https") && args[1].contains("youtube.com") && args[1].contains("v=")) {
            ytd = new Youtube(args[1]);
            if(ytd.getVideo()) {
                ytd.autoChooseAnyFormat(null);
                console.info(" "+ytd.getDownloadLink());
            }
        }else if(args.length == 3 && (args[1].equalsIgnoreCase("any")
                || args[1].equalsIgnoreCase("best")
                || args[1].equalsIgnoreCase("good")
                || args[1].equalsIgnoreCase("medium")
                || args[1].equalsIgnoreCase("poor"))
                && args[2].startsWith("https") && args[2].contains("youtube.com") && args[2].contains("v=")) {
            ytd = new Youtube(args[2]);
            if(ytd.getVideo()) {
                ytd.autoChooseAnyFormat(args[1]);
                console.info(" "+ytd.getDownloadLink());
            }
        }else if(args.length < 2)
            console.info("You have to enter the youtube video link.");
        else
            console.info("This entered argument it's not a valid youtube video link.");
    }

    static void shortLink(String[] args) {
        if(Bitly.canUse) {
            if(args.length > 1 && args[1].length() > 10
                    && args[1].startsWith("http") && args[1].contains(".") && args[1].contains("/")) {
                String shortLink = new Bitly(args[1]).getShort();
                if(shortLink == null || shortLink.equals(args[1]) || shortLink.isEmpty())
                    console.info("Can't short link, maybe it's already short enouth.");
                else console.info("Smallest link: "+shortLink);
            }else console.info("This entered argument it's not a valid link.");
        }else console.info("Bitly is not enabled, check config file.");
    }

    static void context(@NotNull String[] args) {
        if(args.length > 1) {
            if(args[1].equalsIgnoreCase("clear")) {
                List<UUID> contexts = new ArrayList<>(ContextStorage.all().keySet());
                contexts.forEach(ContextStorage::remove);
            }
        }else {
            console.info("-------------------------- Contexts ---------------------------");
            ContextStorage.all().forEach((identifier, context) -> console.info("Context " + identifier + " expire in " + (context.getExpire().remaining() / 1000) + "s."));
            console.info("---------------------------------------------------------------");
        }
    }

    @NotNull
    static Message messageBuilder(String text) {
        UUID context = UUID.nameUUIDFromBytes("willy-console".getBytes());
        ContextStorage.of(context).setSender(new CommandSender());
        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setContent(text);
        message.setText(text);
        message.setFrom("Console");
        message.setTo(Willy.getWilly().getName());
        MessageStorage.add(message);
        return message;
    }

    static void talk(@NotNull String[] args) {
        if(args.length > 2 && args[1].equalsIgnoreCase("debug")) {
            StringBuilder text = new StringBuilder();
            for(int i = 2; i < args.length; i++)
                text.append(" ").append(args[i]);
            Message message = messageBuilder(text.toString().trim());
            new WatsonSender(message.getContext()).sendText(message.getText());
        }else if(args.length > 1) {
            StringBuilder text = new StringBuilder();
            for(int i = 1; i < args.length; i++)
                text.append(" ").append(args[i]);
            Message message = messageBuilder(text.toString().trim());
            new WatsonSender(message.getContext()).sendText(message.getText());
        }else {
            console.info("You need to type a message after 'talk' command.");
        }
    }

    static void player(@NotNull String[] args) {
        if(args.length > 1) {
            VoiceChannel channel = (VoiceChannel)Discord.getBotGateway().getChannelById(Snowflake.of(Config.getString("ap.command_default_channel_id"))).block();
            if(channel == null)
                return;
            DiscordPlayer player = DiscordPlayer.createDiscordPlayer(channel);
            if(args.length > 2 && args[1].equalsIgnoreCase("add")) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    static void noadm(@NotNull String[] args) {
        if(args.length > 1) {
            if(args.length > 3 && args[1].equalsIgnoreCase("ban")) {
                String reason = "";
                for(int i = 4; i < args.length; reason += args[i++]);
                DiscordNoADM.ban(args[2], args[3], reason);
            }else if(args[1].equalsIgnoreCase("unban")) {
                String reason = "";
                for(int i = 4; i < args.length; reason += args[i++]);
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

    static void user(@NotNull String[] args) {
        if(args.length > 1) {
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

    static void whatsapp(@NotNull String[] args) {
        if(args.length < 2) {
            console.info("This command need an user id, type 'help' to see usage.");
            return;
        }
        if(args[1].equals("connect"))
            new Whatsapp().connect();
        if(args[1].equals("disconnect"))
            new Whatsapp().disconnect();
        if(args[1].equals("reconnect"))
            Whatsapp.reconnect();
        if(args[1].equals("logout"))
            Whatsapp.logout();
        if(args[1].equals("chats"))
            Whatsapp.chats();
        if(args[1].equals("talk") && args.length > 3) {
            Whatsapp.getApi().store().findChatByJid(ContactJid.of(args[2])).ifPresent(chat -> {
                try {
                    Whatsapp.getApi().sendMessage(chat, args[3]).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }else {unknow();}
    }

    static void openai(@NotNull String[] args) {
        if(args.length < 2) {
            console.info("This command needs a message, type 'help' to see usage.");
            return;
        }
        StringBuilder text = new StringBuilder();
        for(int i = 1; i < args.length; i++)
            text.append(" ").append(args[i]);
        Message message = messageBuilder(text.toString().trim());
        new OpenAiSender(message.getContext()).sendText(message.getText());
    }

    static void help() {
        console.info("---------------------------------------------------------------");
        console.info("---                        Help Page                        ---");
        console.info("---------------------------------------------------------------");
        console.info("status           : Show Willy general status.");
        console.info("stop             : Close all connections and stop Willy.");
        console.info("talk             : Send a message to Willy, response will be printed.");
        console.info("talk debug       : Send a message to Willy, full debug response will be printed.");
        console.info("contexts         : List all contexts existent.");
        console.info("help             : Show all know commands and their descriptions.");
        console.info("player add       : Add on queue and play music on default channel.");
        console.info("player play      : Play music on player instance.");
        console.info("player resume    : Play music on player instance.");
        console.info("player pause     : Pause music on player instance.");
        console.info("player stop      : Stop music on player instance.");
        console.info("player next      : Go to next music on queue.");
        console.info("player clear     : Clear queue of player instance.");
        console.info("player destroy   : Destroy the player instance.");
        console.info("player info      : See info about the player instance.");
        console.info("user             : Show all info about an Discord user.");
        console.info("noadm ban        : Ban a member.");
        console.info("noadm unban      : Unban a member.");
        console.info("noadm op         : Give OP to a member.");
        console.info("noadm deop       : Remove OP of a member.");
        console.info("noadm mperms     : Show all member permissions.");
        console.info("noadm rperms     : Show all role permissions.");
        console.info("noadm cmperms    : Show all member permissions overwrite on channel.");
        console.info("noadm crperms    : Show all role permissions overwrite on channel.");
        console.info("whats connect    : Connect to WhatsApp and get the QR-Code if needed.");
        console.info("whats disconnect : Disconnected from WhatsApp.");
        console.info("whats reconnect  : Disconnect and reconnect again to WhatsApp.");
        console.info("whats logout     : Logout from a WhatsApp device.");
        console.info("whats chats      : Lists all WhatsApp chats.");
        console.info("---------------------------------------------------------------");
    }

    static void unknow() {
        console.info("Unknow command, to get help type 'help'.");
    }

}
