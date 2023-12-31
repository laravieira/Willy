package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import org.apache.poi.ss.formula.eval.NotImplementedException;

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
