package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyChannel;
import me.laravieira.willy.WillyMessage;

import java.io.File;
import java.util.UUID;

public class TelegramChannel implements WillyChannel {
    private final Chat chat;
    private Context context;

    public TelegramChannel(Chat channel) {
        this.chat = channel;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void send(WillyMessage message) {
        sendText(message.getText());
    }

    @Override
    public void sendLast() {
        send(context.getMessages().getLast());
    }

    public void sendText(String message) {
        try {
            SendMessage send = new SendMessage(chat.id(), message);
            Telegram.getBot().execute(send);
            Willy.getLogger().fine("Telegram send text success.");
        } catch (Exception e) {
            Willy.getLogger().warning("Telegram send text fail: "+e.getMessage());
        }
    }

    public void sendLink(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendStick(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendGif(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

//    public void sendImage(WillyMessage message) {
//        try {
//            List<byte[]> images = new ArrayList<>();
//
//            // Get the bytes from the files and urls
//            for(File file : message.getAttachments()) {
//                images.add(Files.readAllBytes(file.toPath()));
//            }
//            for(String url : message.getUrls()) {
//                try(InputStream stream = new URI(url).toURL().openStream()) {
//                    images.add(stream.readAllBytes());
//                }
//            }
//
//            // Send the images
//            for(byte[] image : images) {
//                SendPhoto send = new SendPhoto(chat.id(), image)
//                        .caption(message.getText());
//                Telegram.getBot().execute(send);
//                Willy.getLogger().fine("Telegram send image success: "+message.getId());
//            }
//        } catch (Exception e) {
//            Willy.getLogger().warning("Telegram send image fail: "+e.getMessage());
//        }
//    }

    public void sendVideo(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendAudio(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendLocation(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendContact(WillyMessage message) throws Exception {
        throw new Exception("This function is not implemented.");
    }

    public void sendFile(File message) throws Exception {
        throw new Exception("This function is not implemented.");
    }
}
