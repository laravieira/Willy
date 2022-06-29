package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.User;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;

import java.util.UUID;

public class DiscordMessage extends Message {
    private final discord4j.core.object.entity.Message message;

    public DiscordMessage(UUID context, User user, discord4j.core.object.entity.Message message, long expire) {
        super(context);
        this.message = message;
        this.content = message.getContent();
        this.text = message.getContent();
        this.from = user.getUsername();
        this.to = Willy.getWilly().getName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public DiscordMessage(UUID context, Message message, discord4j.core.object.entity.Message result, long expire) {
        super(context);
        this.message = result;
        this.content = message.getContent();
        this.text = message.getText();
        this.from = message.getFrom();
        this.to = message.getTo();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    @Override
    public void delete() {
        try {
            message.delete("Willy auto deleted this message.").block();
        }catch (RuntimeException ignore) {}
    }
}
