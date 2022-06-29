package me.laravieira.willy.chat.watson;

import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;

import java.io.File;
import java.util.UUID;

public class WatsonSender implements SenderInterface {
    private final UUID context;

    public WatsonSender(UUID context) {
        this.context = context;
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        ContextStorage.of(context).getWatson().getWatsonMessager().sendTextMessage(message);
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
