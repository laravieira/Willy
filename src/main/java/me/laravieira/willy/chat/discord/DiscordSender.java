package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.UUID;

public class DiscordSender implements SenderInterface {
    private final UUID context;
    private final MessageChannel channel;
    private final long expire;

    public DiscordSender(UUID context, MessageChannel channel, long expire) {
        this.context = context;
        this.channel = channel;
        this.expire = expire;
    }

    private void saveMessage(discord4j.core.object.entity.Message result) {
        Message lastMessage = ContextStorage.of(context).getLastMessage();
        DiscordMessage message = new DiscordMessage(context, lastMessage, result, expire);
        MessageStorage.remove(lastMessage.getId());
        MessageStorage.add(message);
    }

    private void sendMessage(MessageCreateSpec messageCreateSpec) {
        discord4j.core.object.entity.Message result;
        try {
            result = channel.createMessage(messageCreateSpec).block();
        }catch (NullPointerException e) {
            new Discord().connect();
            result = channel.createMessage(messageCreateSpec).block();
        }
        saveMessage(result);
    }

    @Override
    public void send(Object message) {
    }

    @Override
    public void sendText(String message) {
        sendMessage(MessageCreateSpec.builder().content(message).build());
    }

    @Override
    public void sendLink(@NotNull Message message) {
        String content = (String)message.getContent();
        sendMessage(MessageCreateSpec.builder().content(content).build());
    }

    @Override
    public void sendStick(Message message) {
        //TODO Implement how to send stick images to Discord
//        try {
//            sendMessage(MessageCreateSpec.builder()
//                    .addEmbed(EmbedCreateSpec.builder()
//                            .thumbnail("attachment://stick.png")
//                            .build())
//                    .addFile("stick.png", new FileInputStream((File) message.getContent()))
//                    .build());
//        } catch(FileNotFoundException e) {
//            e.printStackTrace();
//        }
        throw new NotImplementedException("This function is not implemented.");
    }

    public void sendEmbed(MessageCreateSpec messageCreateSpec) {
        sendMessage(messageCreateSpec);
    }

    @Override
    public void sendGif(Message message) {
        //TODO Implement how to send gif images to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendImage(Message message) {
        //TODO Implement how to send images to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendVideo(Message message) {
        //TODO Implement how to send videos to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendAudio(Message message) {
        //TODO Implement how to send audio to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendLocation(Message message) {
        //TODO Implement how to send locations to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendContact(Message message) {
        //TODO Implement how to send contacts to Discord
        throw new NotImplementedException("This function is not implemented.");
    }

    @Override
    public void sendFile(File message) {
        //TODO Validate files are sent correctly to Discord
        try {
            sendMessage(MessageCreateSpec.builder()
                .addFile(message.getName(), new FileInputStream(message))
                .build());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
