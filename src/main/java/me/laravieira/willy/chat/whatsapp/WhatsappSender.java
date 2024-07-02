package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.contact.ContactStatus;
import it.auties.whatsapp.model.message.standard.ImageMessage;
import it.auties.whatsapp.model.message.standard.ImageMessageSimpleBuilder;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class WhatsappSender implements SenderInterface {
    private final Chat chat;

    public WhatsappSender(Chat chat) {
        this.chat = chat;
    }

    private void resetStatus() {
        Thread messageStatusUpdate = new Thread(() -> {
            try {
                Whatsapp.getApi().changePresence(chat, ContactStatus.AVAILABLE).get();
            }catch(CompletionException | InterruptedException | ExecutionException e) {
                Willy.getLogger().fine(STR."Whatsapp status reset fail: \{e.getMessage()}");
            }
        });
        messageStatusUpdate.setDaemon(true);
        messageStatusUpdate.start();
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
            Whatsapp.getApi().sendMessage(chat, message).get();
            resetStatus();
        } catch (InterruptedException | ExecutionException e) {
            Willy.getLogger().warning(STR."Whatsapp send text fail: \{e.getMessage()}");
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

            // Send the images as separate messages
            for (byte[] image : images) {
                ImageMessage img = new ImageMessageSimpleBuilder()
                        .media(image)
                        .caption(message.getText())
                        .build();
                Whatsapp.getApi().sendMessage(chat, img).get();
                Willy.getLogger().fine(STR."Whatsapp send image success: \{message.getId()}");
            }
            resetStatus();
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Whatsapp send image fail: \{e.getMessage()}");
        }
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
