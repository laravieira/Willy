package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.WillyChannel;

import java.io.File;
import java.io.FileInputStream;

public class DiscordChannel implements WillyChannel {
    private Context context;
    private final MessageChannel channel;
    private final long expire;

    public DiscordChannel(MessageChannel channel, long expire) {
        this.channel = channel;
        this.expire = expire;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void send(WillyMessage message) {
        if(message.getContent() instanceof MessageCreateSpec)
            sendMessage((MessageCreateSpec) message.getContent());
        else sendText(message.getText());
    }

    @Override
    public void sendLast() {
        WillyMessage lastMessage = context.getMessages().getLast();
        send(lastMessage);
    }

    public void sendText(String message) {
        try {
            sendMessage(MessageCreateSpec.builder().content(message).build());
            Willy.getLogger().fine("Discord send text success.");
        } catch (Exception e) {
            Willy.getLogger().warning("Discord send text fail: "+e.getMessage());
        }
    }

    private void sendMessage(MessageCreateSpec messageCreateSpec) {
        channel.createMessage(messageCreateSpec)
            .doOnSuccess(msg -> {
                // Save the message to the storage for auto delete
                WillyMessage lastMessage = context.getMessages().getLast();
                DiscordMessage message = new DiscordMessage(lastMessage, msg, expire);
                context.getMessages().removeLast();
                context.getMessages().add(message);
            })
            .doOnError(err -> Willy.getLogger().severe("Discord sendMessage failed: "+err.getMessage()))
            .subscribe();
    }

//    public void sendLink(@NotNull WillyMessage message) {
//        try {
//            String content = (String)message.getContent();
//            sendMessage(MessageCreateSpec.builder().content(content).build());
//            Willy.getLogger().fine("Discord send link success.");
//        } catch (Exception e) {
//            Willy.getLogger().warning("Discord send link fail: "+e.getMessage());
//        }
//    }
//
//    public void sendStick(Message message) throws Exception {
//        //TODO Implement how to send stick images to Discord
//        throw new Exception("This function is not implemented.");
//    }
//
//
//    public void sendGif(Message message) throws Exception {
//        //TODO Implement how to send gif images to Discord
//        throw new Exception("This function is not implemented.");
//    }
//
    public void sendImage(WillyMessage message) {
        try {
            StringBuilder links = new StringBuilder(message.getText());
            for(String url : message.getUrls()) {
                links.append("\r\n").append(url);
            }

            MessageCreateSpec.Builder builder = MessageCreateSpec.builder();
            builder.content(links.toString());

            for(File file : message.getImages())
//                builder.addFile(file.getName(), new FileInputStream(file));

            sendMessage(builder.build());
            Willy.getLogger().fine("Discord send image success.");
        } catch (Exception e) {
            Willy.getLogger().warning("Discord send image fail: "+e.getMessage());
        }
    }
//
//    public void sendFile(File message) {
//        //TODO Validate files are sent correctly to Discord
//        try {
//            sendMessage(MessageCreateSpec.builder()
//                .addFile(message.getName(), new FileInputStream(message))
//                .build());
//            Willy.getLogger().fine("Discord send file success.");
//        } catch (Exception e) {
//            Willy.getLogger().warning("Discord send file fail: "+e.getMessage());
//        }
//    }
}
