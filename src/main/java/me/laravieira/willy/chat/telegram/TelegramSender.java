package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;

public class TelegramSender implements SenderInterface {
    private final Chat chat;

    public TelegramSender(Chat channel) {
        this.chat = channel;
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
    public void sendLink(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendStick(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendGif(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendImage(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendVideo(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendAudio(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendLocation(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendContact(Message message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendFile(File message) throws Exception {
        throw new Exception("This function is not implemented.");
    }
}
