package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.model.chat.Chat;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class WhatsappSender implements SenderInterface {
    private final UUID context;
    private final Chat chat;
    private final long expire;

    public WhatsappSender(UUID context, Chat chat, long expire) {
        this.context = context;
        this.chat = chat;
        this.expire = expire;
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        try {
            Whatsapp.getApi().sendMessage(chat, message).get();
        } catch (InterruptedException | ExecutionException e) {
            Willy.getLogger().warning("Fail when sending Whatsapp message.");
        }
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
