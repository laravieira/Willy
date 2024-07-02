package me.laravieira.willy.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;

public class CommandSender implements SenderInterface {
    private final ChatInputInteractionEvent event;

    public CommandSender(ChatInputInteractionEvent event) {
        this.event = event;
    }

    @Override
    public void send(Message message) {

    }

    @Override
    public void sendText(String message) {
        event.reply(message);
    }

    @Override
    public void sendLink(Message message) {

    }

    @Override
    public void sendStick(Message message) {

    }

    @Override
    public void sendGif(Message message) {

    }

    @Override
    public void sendImage(Message message) {

    }

    @Override
    public void sendVideo(Message message) {

    }

    @Override
    public void sendAudio(Message message) {

    }

    @Override
    public void sendLocation(Message message) {

    }

    @Override
    public void sendContact(Message message) {

    }

    @Override
    public void sendFile(File message) {

    }
}
