package me.laravieira.willy.chat.command;

import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;

public class CommandSender implements SenderInterface {
    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        Willy.getLogger().info(message);
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