package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.utils.PassedInterval;

public class DiscordMessage extends WillyMessage {
    private final Message message;

    public DiscordMessage(User user, Message message, String text, long expire) {
        super(message.getContent());
        this.from =  user.getUsername();
        this.message = message;
        this.text = text;
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public DiscordMessage(WillyMessage message, Message result, long expire) {
        super(message.getContent());
        this.context = message.getContext();
        this.message = result;
        this.from = message.getFrom();
        this.to = message.getTo();
        this.text = message.getText();
        this.images = message.getImages();
        this.audios = message.getAudios();
        this.documents = message.getDocuments();
        this.urls = message.getUrls();
        this.created = message.getCreated();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public void destroy() {
        super.destroy();
        try {
            message.delete("Willy auto deleted this message.").block();
            Willy.getLogger().fine("Discord auto deleted message "+message.getId().asLong()+" at "+message.getChannelId().asLong()+".");
        }catch (RuntimeException ignore) {}
    }
}
