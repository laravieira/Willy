package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommandWhatsapp implements CommandListener {
    public static final String COMMAND = "whats";
    public static final String DESCRIPTION = "Manage whatsapp connection.";
    public static final String OPTION_CONNECT = "connect";
    public static final String OPTION_DISCONNECT = "disconnect";
    public static final String OPTION_RECONNECT = "reconnect";
    public static final String OPTION_LOGOUT = "logout";
    public static final String OPTION_CONNECTIONS = "connections";
    public static final String OPTION_LOGOUT_ALL = "logoutall";
    public static final String OPTION_CHATS = "chats";
    public static final String OPTION_TALK = "talk";
    public static final String OPTION_TALK_NUMBER = "number";
    public static final String OPTION_TALK_MESSAGE = "message";

    @Override
    public ApplicationCommandRequest register() {
        List<ApplicationCommandOptionChoiceData> options = new ArrayList<>();
        options.add(registerChoice(OPTION_CONNECT, OPTION_CONNECT));
        options.add(registerChoice(OPTION_DISCONNECT, OPTION_DISCONNECT));
        options.add(registerChoice(OPTION_RECONNECT, OPTION_RECONNECT));
        options.add(registerChoice(OPTION_LOGOUT, OPTION_LOGOUT));
        options.add(registerChoice(OPTION_LOGOUT_ALL, OPTION_LOGOUT_ALL));
        options.add(registerChoice(OPTION_CONNECTIONS, OPTION_CONNECTIONS));
        options.add(registerChoice(OPTION_CHATS, OPTION_CHATS));
        options.add(registerChoice(OPTION_TALK, OPTION_TALK));

        return ApplicationCommandRequest.builder()
                .name(COMMAND)
                .description(DESCRIPTION)
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description(DESCRIPTION)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addAllChoices(options)
                        .required(true)
                        .addOption(ApplicationCommandOptionData.builder()
                                .name(OPTION_TALK_NUMBER)
                                .description(DESCRIPTION)
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .required(false)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name(OPTION_TALK_MESSAGE)
                                .description(DESCRIPTION)
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .required(false)
                                .build())
                        .build())
                .build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        event.getOption("command").ifPresent(
            command -> command.getValue().ifPresent(value -> {
                switch (value.asString()) {
                    case OPTION_CONNECT -> onConnect(event);
                    case OPTION_DISCONNECT -> onDisconnect(event);
                    case OPTION_RECONNECT -> onReconnect(event);
                    case OPTION_LOGOUT -> onLogout(event);
                    case OPTION_LOGOUT_ALL -> onLogoutAll(event);
                    case OPTION_CONNECTIONS -> onConnections(event);
                    case OPTION_CHATS -> onChats(event);
                    case OPTION_TALK -> onTalk(event, command);
                }
            })
        );
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

    private void onConnect(@NotNull ChatInputInteractionEvent event) {
        Whatsapp.create();
        event.reply("Whatsapp connect sent.").subscribe();
    }

    private void onDisconnect(@NotNull ChatInputInteractionEvent event) {
        new Whatsapp().disconnect();
        event.reply("Whatsapp disconnect sent.").subscribe();
    }

    private void onReconnect(@NotNull ChatInputInteractionEvent event) {
        Whatsapp.reconnect();
        event.reply("Whatsapp reconnect sent.").subscribe();
    }

    private void onLogout(@NotNull ChatInputInteractionEvent event) {
        Whatsapp.logout();
        new Whatsapp().disconnect();
        event.reply("Whatsapp logout sent.").subscribe();
    }

    private void onLogoutAll(@NotNull ChatInputInteractionEvent event) {
        event.reply("Loading...").subscribe();
        StringBuilder list = new StringBuilder();
        list.append("```yaml\r\n");
        Whatsapp.logout();
        new Whatsapp().disconnect();
        Whatsapp.getApi().awaitDisconnection();
//        try {
//            Files.walk(Preferences.home()).forEach(folder -> {
//                try {
//                    if(Files.isDirectory(folder))
//                        for(Path file : Files.walk(folder).toList())
//                            Files.deleteIfExists(file);
//                    Files.deleteIfExists(folder);
//                    StringBuilder message = new StringBuilder(list.append(folder.getFileName()).append(": deleted\r\n"));
//                    event.editReply(message.append("```").toString()).subscribe();
//                }catch(IOException ignore) {
//                    StringBuilder message = new StringBuilder(list.append(folder.getFileName()).append(": failed\r\n"));
//                    event.editReply(message.append("```").toString()).subscribe();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        event.editReply(list.append("```:white_check_mark:").toString()).subscribe();
    }

    private void onConnections(@NotNull ChatInputInteractionEvent event) {
        StringBuilder list = new StringBuilder();
        event.reply("Loading...").subscribe();
        list.append("**Whatsapp connections** `").append(new Date()).append("`").append("\r\n");
        list.append("```yaml\r\n").append("\r\n");
        list.append("```");
        event.editReply(list.toString()).subscribe();
    }

    private void onChats(@NotNull ChatInputInteractionEvent event) {
        Whatsapp.getApi().store().chats()
                .forEach(chat -> event.reply(chat.name()));
    }

    private void onTalk(@NotNull ChatInputInteractionEvent event, @NotNull ApplicationCommandInteractionOption option) {
//        option.getOption(OPTION_TALK_NUMBER).ifPresentOrElse(
//            number -> option.getOption(OPTION_TALK_MESSAGE).ifPresentOrElse(
//                message -> {
//                    String numberValue = number.getValue().isPresent() ? number.getValue().get().asString() : "";
//                    String messageValue = message.getValue().isPresent() ? message.getValue().get().asString() : "";
//                    Whatsapp.getApi().store().findChatByJid(ContactJid.of(numberValue)).ifPresent(chat -> {
//                        try {
//                            Whatsapp.getApi().sendMessage(chat, messageValue).get();
//                        } catch (InterruptedException | ExecutionException e) {
//                            event.reply(e.getMessage()).subscribe();
//                        }
//                    });
//                }, () -> event.reply("missing the message.").subscribe().dispose()
//            ), () -> event.reply("Missing the number.").subscribe().dispose()
//        );
    }
}
