package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.util.UUID;

public class TelegramSender implements SenderInterface {
    private final UUID context;
    private final Chat chat;
    private final long expire;

    public TelegramSender(UUID context, Chat channel, long expire) {
        this.context = context;
        this.chat = channel;
        this.expire = expire;
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        SendMessage send = new SendMessage(chat.id(), message);
        Telegram.getBot().execute(send);
    }

    @Override
    public void sendLink(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendStick(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendGif(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendImage(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendVideo(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendAudio(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendLocation(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendContact(Message message) {
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendFile(File message) {
        throw new NotImplementedException("This function is not implemented.");
    }
}
