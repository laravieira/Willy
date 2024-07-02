package me.laravieira.willy.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import it.auties.whatsapp.controller.StoreBuilder;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

public class CommandWhatsapp implements CommandListener {
    public static final String COMMAND = "whats";
    public static final String DESCRIPTION = "Manage whatsapp connection.";
    public static final String OPTION_CONNECT = "connect";
    public static final String OPTION_DISCONNECT = "disconnect";
    public static final String OPTION_RESET = "reset";
    public static final String OPTION_LOGOUT = "logout";
    public static final String OPTION_CHATS = "chats";
    public static final String OPTION_TALK = "talk";
    public static final String OPTION_TALK_NUMBER = "number";
    public static final String OPTION_TALK_MESSAGE = "message";

    @Override
    public ApplicationCommandRequest register() {
        List<ApplicationCommandOptionChoiceData> options = new ArrayList<>();
        options.add(registerChoice(OPTION_CONNECT, OPTION_CONNECT));
        options.add(registerChoice(OPTION_DISCONNECT, OPTION_DISCONNECT));
        options.add(registerChoice(OPTION_RESET, OPTION_RESET));
        options.add(registerChoice(OPTION_LOGOUT, OPTION_LOGOUT));
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
                    case OPTION_RESET -> onReset(event);
                    case OPTION_LOGOUT -> onLogout(event);
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

    private void onReset(@NotNull ChatInputInteractionEvent event) {
        event.reply("Hard reset WhatsApp").subscribe();
        try {
            Whatsapp.logout();
            Files.delete(Paths.get(System.getProperty("user.home"), ".cobalt", "web", "Willy"));
            event.editReply("Whatsapp reset.").subscribe();
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Error on hard reset WhatsApp: \{e.getMessage()}");
            event.editReply(STR."Whatsapp reset: \{e.getMessage()}").subscribe();
        }
    }

        private void onLogout(@NotNull ChatInputInteractionEvent event) {
        Whatsapp.logout();
        new Whatsapp().disconnect();
        event.reply("Whatsapp logout sent.").subscribe();
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
