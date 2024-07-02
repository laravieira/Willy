package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
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

    private void sendMessage(MessageCreateSpec messageCreateSpec) {
        discord4j.core.object.entity.Message result;
        try {
            result = channel.createMessage(messageCreateSpec).block();
        }catch (NullPointerException e) {
            new Discord().connect();
            result = channel.createMessage(messageCreateSpec).block();
        }
        // Save the message to the storage for auto delete
        Message lastMessage = ContextStorage.of(context).getLastMessage();
        DiscordMessage message = new DiscordMessage(context, lastMessage, result, expire);
        MessageStorage.remove(lastMessage.getId());
        MessageStorage.add(message);
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
            sendMessage(MessageCreateSpec.builder().content(message).build());
            Willy.getLogger().fine("Discord send text success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Discord send text fail: \{e.getMessage()}");
        }
    }

    @Override
    public void sendLink(@NotNull Message message) {
        try {
            String content = (String)message.getContent();
            sendMessage(MessageCreateSpec.builder().content(content).build());
            Willy.getLogger().fine("Discord send link success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Discord send link fail: \{e.getMessage()}");
        }
    }

    @Override
    public void sendStick(Message message) throws Exception {
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
        throw new Exception("This function is not implemented.");
    }

    public void sendEmbed(MessageCreateSpec messageCreateSpec) {
        try {
            sendMessage(messageCreateSpec);
            Willy.getLogger().fine("Discord send embed success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Discord send embed fail: \{e.getMessage()}");
        }
    }

    @Override
    public void sendGif(Message message) throws Exception {
        //TODO Implement how to send gif images to Discord
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendImage(Message message) {
        try {
            StringBuilder links = new StringBuilder(message.getText());
            for(String url : message.getUrls()) {
                links.append("\r\n").append(url);
            }

            MessageCreateSpec.Builder builder = MessageCreateSpec.builder();
            builder.content(links.toString());

            for(File file : message.getAttachments()) {
                builder.addFile(file.getName(), new FileInputStream(file));
            }

            sendMessage(builder.build());
            Willy.getLogger().fine("Discord send image success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Discord send image fail: \{e.getMessage()}");
        }
    }

    @Override
    public void sendVideo(Message message) throws Exception {
        //TODO Implement how to send videos to Discord
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendAudio(Message message) throws Exception {
        //TODO Implement how to send audio to Discord
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendLocation(Message message) throws Exception {
        //TODO Implement how to send locations to Discord
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendContact(Message message) throws Exception {
        //TODO Implement how to send contacts to Discord
        throw new Exception("This function is not implemented.");
    }

    @Override
    public void sendFile(File message) {
        //TODO Validate files are sent correctly to Discord
        try {
            sendMessage(MessageCreateSpec.builder()
                .addFile(message.getName(), new FileInputStream(message))
                .build());
            Willy.getLogger().fine("Discord send file success.");
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Discord send file fail: \{e.getMessage()}");
        }
    }
}
