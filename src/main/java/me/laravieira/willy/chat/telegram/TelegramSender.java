package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TelegramSender implements SenderInterface {
    private final Chat chat;

    public TelegramSender(Chat channel) {
        this.chat = channel;
    }

    @Override
    public void send(Message message) {
        switch (message.getType()) {
            case IMAGE:
                sendImage(message);
                break;
            case TEXT:
            default:
                sendText(message.getText());
                break;
        }
    }

    @Override
    public void sendText(String message) {
        try {
            SendMessage send = new SendMessage(chat.id(), message);
            Telegram.getBot().execute(send);
            Willy.getLogger().fine("Telegram send text success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Telegram send text fail: \{e.getMessage()}");
        }
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
    public void sendImage(Message message) {
        try {
            List<byte[]> images = new ArrayList<>();

            // Get the bytes from the files and urls
            for(File file : message.getAttachments()) {
                images.add(Files.readAllBytes(file.toPath()));
            }
            for(String url : message.getUrls()) {
                try(InputStream stream = new URI(url).toURL().openStream()) {
                    images.add(stream.readAllBytes());
                }
            }

            // Send the images
            for(byte[] image : images) {
                SendPhoto send = new SendPhoto(chat.id(), image)
                        .caption(message.getText());
                Telegram.getBot().execute(send);
                Willy.getLogger().fine(STR."Telegram send image success: \{message.getId()}");
            }
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Telegram send image fail: \{e.getMessage()}");
        }
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
