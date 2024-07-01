package me.laravieira.willy.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.command.CommandListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandDiscord implements CommandListener {
    public static final String COMMAND = "discord";
    public static final String DESCRIPTION = "Make discord commands";
    public static final String OPTION = "command";
    public static final String OPTION_USER = "user";

    public static final String OPTION_NOADM = "noadm";
    public static final String OPTION_NOADM_BAN = "ban";
    public static final String OPTION_NOADM_UNBAN = "unban";
    public static final String OPTION_NOADM_OP = "op";
    public static final String OPTION_NOADM_DEOP = "deop";
    public static final String OPTION_NOADM_MEMBER_PERMISSIONS = "m-perms";
    public static final String OPTION_NOADM_REMOVE_PERMISSIONS = "rm-perms";
    public static final String OPTION_NOADM_CHANNEL_PERMISSIONS = "ch-perms";
    public static final String OPTION_NOADM_ROLE_PERMISSIONS = "role-perms";

    public static final String OPTION_PLAYER = "player";
    public static final String OPTION_PLAYER_ADD = "add";
    public static final String OPTION_PLAYER_PLAY = "play";
    public static final String OPTION_PLAYER_RESUME = "resume";
    public static final String OPTION_PLAYER_PAUSE = "pause";
    public static final String OPTION_PLAYER_STOP = "stop";
    public static final String OPTION_PLAYER_NEXT = "next";
    public static final String OPTION_PLAYER_CLEAR = "clear";
    public static final String OPTION_PLAYER_DESTROY = "destroy";
    public static final String OPTION_PLAYER_INFO = "info";

    @Override
    public ApplicationCommandRequest register() {
        List<ApplicationCommandOptionChoiceData> options = new ArrayList<>();
        options.add(registerChoice(OPTION_USER, OPTION_USER));
        options.add(registerChoice(OPTION_NOADM, OPTION_NOADM));
        options.add(registerChoice(OPTION_PLAYER, OPTION_PLAYER));

        List<ApplicationCommandOptionChoiceData> noadmOptions = new ArrayList<>();
        options.add(registerChoice(OPTION_NOADM_BAN, OPTION_NOADM_BAN));
        options.add(registerChoice(OPTION_NOADM_UNBAN, OPTION_NOADM_UNBAN));
        options.add(registerChoice(OPTION_NOADM_OP, OPTION_NOADM_OP));
        options.add(registerChoice(OPTION_NOADM_DEOP, OPTION_NOADM_DEOP));
        options.add(registerChoice(OPTION_NOADM_MEMBER_PERMISSIONS, OPTION_NOADM_MEMBER_PERMISSIONS));
        options.add(registerChoice(OPTION_NOADM_REMOVE_PERMISSIONS, OPTION_NOADM_REMOVE_PERMISSIONS));
        options.add(registerChoice(OPTION_NOADM_CHANNEL_PERMISSIONS, OPTION_NOADM_CHANNEL_PERMISSIONS));
        options.add(registerChoice(OPTION_NOADM_ROLE_PERMISSIONS, OPTION_NOADM_ROLE_PERMISSIONS));

        List<ApplicationCommandOptionChoiceData> playerOtions = new ArrayList<>();
        options.add(registerChoice(OPTION_PLAYER_ADD, OPTION_PLAYER_ADD));
        options.add(registerChoice(OPTION_PLAYER_PLAY, OPTION_PLAYER_PLAY));
        options.add(registerChoice(OPTION_PLAYER_RESUME, OPTION_PLAYER_RESUME));
        options.add(registerChoice(OPTION_PLAYER_PAUSE, OPTION_PLAYER_PAUSE));
        options.add(registerChoice(OPTION_PLAYER_STOP, OPTION_PLAYER_STOP));
        options.add(registerChoice(OPTION_PLAYER_NEXT, OPTION_PLAYER_NEXT));
        options.add(registerChoice(OPTION_PLAYER_CLEAR, OPTION_PLAYER_CLEAR));
        options.add(registerChoice(OPTION_PLAYER_DESTROY, OPTION_PLAYER_DESTROY));
        options.add(registerChoice(OPTION_PLAYER_INFO, OPTION_PLAYER_INFO));

        return ApplicationCommandRequest.builder()
                .name(COMMAND)
                .description(DESCRIPTION)
                .addOption(ApplicationCommandOptionData.builder()
                        .name(OPTION)
                        .addAllChoices(options)
                        .description(DESCRIPTION)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .addOption(ApplicationCommandOptionData.builder()
                                .name(OPTION_USER)
                                .description(DESCRIPTION)
                                .type(ApplicationCommandOption.Type.USER.getValue())
                                .required(false)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name(OPTION_NOADM)
                                .description(DESCRIPTION)
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .addAllChoices(noadmOptions)
                                .required(false)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name(OPTION_PLAYER)
                                .description(DESCRIPTION)
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .addAllChoices(playerOtions)
                                .required(false)
                                .build())
                        .build())
                .build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        event.getOption(OPTION).flatMap(ApplicationCommandInteractionOption::getValue).ifPresent(choice -> {
            switch (choice.asString()) {
                case OPTION_USER -> onUserCommand(event);
                case OPTION_NOADM -> onNoADMCommand(event);
                case OPTION_PLAYER -> onPlayerCommand(event);
                default -> event.reply("Unknown Discord command.").subscribe();
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

    private ApplicationCommandOptionChoiceData registerChoice(@NotNull String name, @NotNull String value) {
        return ApplicationCommandOptionChoiceData.builder()
                .name(name)
                .value(value)
                .build();
    }

    private void onUserCommand(@NotNull ChatInputInteractionEvent event) {
        event.getOption(OPTION).ifPresent(
            command -> command.getOption(OPTION_USER).ifPresentOrElse(
                option -> option.getValue().ifPresent(value -> {
                    User user = value.asUser().block();
                    if(user == null) {
                        event.reply("Invalid user.").subscribe();
                        return;
                    }

                    StringBuilder list = new StringBuilder();
                    list.append("**Discord user** `").append(new Date()).append("`").append("\r\n");
                    list.append("```yaml").append("\r\n");

                    list.append("avatar: ").append(user.getAvatarUrl()).append("\r\n");
                    list.append("   avatar: ").append(user.getAvatarUrl()).append("\r\n");
                    list.append("   default: ").append(user.getDefaultAvatarUrl()).append("\r\n");
                    list.append("discriminator: ").append(user.getDiscriminator()).append("\r\n");
                    list.append("mention: ").append(user.getMention()).append("\r\n");
                    list.append("tag: ").append(user.getTag()).append("\r\n");
                    list.append("username: ").append(user.getUsername()).append("\r\n");
                    list.append("id: ").append(user.getId().asString()).append("\r\n");

                    list.append("```");
                    event.reply(list.toString()).subscribe();
                }),
                () -> event.reply("You need to type an user ID.").subscribe()
            )
        );
    }

    private void onNoADMCommand(@NotNull ChatInputInteractionEvent event) {
        event.reply("Not implemented yet.");
//        if(count > 1) {
//            if(count > 3 && args[1].equalsIgnoreCase("ban")) {
//                String reason = "";
//                for(int i = 4; i < count; reason += args[i++]);
//                DiscordNoADM.ban(args[2], args[3], reason);
//            }else if(args[1].equalsIgnoreCase("unban")) {
//                String reason = "";
//                for(int i = 4; i < count; reason += args[i++]);
//                DiscordNoADM.unban(args[2], args[3], reason);
//            }else if(args[1].equalsIgnoreCase("op")) {
//                DiscordNoADM.op(args[2], args[3]);
//            }else if(args[1].equalsIgnoreCase("deop")) {
//                DiscordNoADM.deop(args[2], args[3]);
//            }else if(args[1].equalsIgnoreCase("mperms")) {
//                DiscordNoADM.listMemberPermissions(args[2], args[3]);
//            }else if(args[1].equalsIgnoreCase("rperms")) {
//                DiscordNoADM.listRolePermissions(args[2], args[3]);
//            }else if(args[1].equalsIgnoreCase("cmperms")) {
//                DiscordNoADM.listChannelMemberPermissions(args[2], args[3], args[4]);
//            }else if(args[1].equalsIgnoreCase("crperms")) {
//                DiscordNoADM.listChannelRolePermissions(args[2], args[3], args[4]);
//            }else {
//                console.info("use: noadm [cmd] [guild_id] [member_id] reason");
//            }
//        }else {
//            console.info("This command need sub command, type 'help' to see usage.");
//        }
    }

    private void onPlayerCommand(@NotNull ChatInputInteractionEvent event) {
        event.reply("Not implemented yet.");
//        if(count > 1) {
//            VoiceChannel channel = (VoiceChannel) Discord.getBotGateway().getChannelById(Snowflake.of(Config.getString("ap.command_default_channel_id"))).block();
//            if(channel == null)
//                return;
//            DiscordPlayer player = DiscordPlayer.createDiscordPlayer(channel);
//            if(count > 2 && args[1].equalsIgnoreCase("add")) {
//                player.add(args[2]);
//            }else if(args[1].equalsIgnoreCase("play")) {
//                player.play();
//            }else if(args[1].equalsIgnoreCase("resume")) {
//                player.resume();
//            }else if(args[1].equalsIgnoreCase("pause")) {
//                player.pause();
//            }else if(args[1].equalsIgnoreCase("stop")) {
//                player.stop();
//            }else if(args[1].equalsIgnoreCase("next")) {
//                player.next();
//            }else if(args[1].equalsIgnoreCase("clear")) {
//                player.clear();
//            }else if(args[1].equalsIgnoreCase("destroy")) {
//                player.destroy();
//            }else if(args[1].equalsIgnoreCase("info")) {
//                console.info("-------------------- Default Player Info ----------------------");
//                console.info("Player status: "+(player.getPlayer().getPlayingTrack() == null?"EMPTY":player.getPlayer().getPlayingTrack().getState().name()));
//                console.info("Playing track: "+(player.getPlayer().getPlayingTrack() == null?"EMPTY":player.getPlayer().getPlayingTrack().getInfo().title));
//                console.info("Next track: "+(player.getTrackScheduler().getNext() == null?"EMPTY":player.getTrackScheduler().getNext().getInfo().title));
//                console.info("Queue size: "+player.getTrackScheduler().getQueue().size());
//                console.info("Channel status: "+(DiscordPlayer.isMemberConnectedTo(player.getChannel(), Snowflake.of(Config.getString("discord.client_id")))?"CONNECTED":"DISCONNECTED"));
//                console.info("Channel identifier: "+player.getChannel().getName()+" ("+player.getChannel().getId().asString()+")");
//                console.info("---------------------------------------------------------------");
//            }else {
//                console.info("Undefined command, type 'help' to see usage.");
//            }
//        }else {
//            console.info("This command need sub command, type 'help' to see usage.");
//        }
    }
}