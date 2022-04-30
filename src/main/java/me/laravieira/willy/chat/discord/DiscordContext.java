package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.Willy;
import me.laravieira.willy.kernel.Context;

import java.util.Date;

public class DiscordContext extends Context {
    private MessageChannel channel;
    private Message message;
    private User user;
    private final DiscordSender sender;

    //TODO Solve willy answering any discord context for an open discord context
    // Willy answer the same person at any chat, ignoring chat change from private to public

    public static DiscordContext getContext(MessageChannel channel, Message message, User user, String id) {
        DiscordContext context;
        if(Context.getContexts().containsKey(id)) {
            context = (DiscordContext) Context.getContexts().get(id);
            context.autoDeleteMessage(message);
            context.setChannel(channel);
            context.setMessage(message);
            context.setUser(user);
        }else {
            context = new DiscordContext(channel, message, user, id);
            Context.getContexts().put(id, context);
        }
        return context;
    }

    public DiscordContext(MessageChannel channel, Message message, User user, String id) {
        super(id);
        autoDeleteMessage(message);
        this.channel = channel;
        this.message = message;
        this.user = user;
        this.sender = new DiscordSender(this);
    }

    public void autoDeleteMessage(Message message) {
        if(Willy.getConfig().asBoolean("discord.clear-public-chats") && getDeleteMessages()) {
            long expire = Willy.getConfig().asTimestamp("discord.clear-after-wait") + new Date().getTime();
            DiscordListener.autoDeleteMessage(expire, message);
        }
    }


    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMessage(Message message) {
        this.message = message;
    }


    public MessageChannel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public Message getMessage() {
        return message;
    }

    public DiscordSender getSender() {
        return sender;
    }
}
