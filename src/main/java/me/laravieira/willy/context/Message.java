package me.laravieira.willy.context;

import lombok.Getter;
import lombok.Setter;
import me.laravieira.willy.utils.PassedInterval;

import java.io.File;
import java.util.*;

@Getter
public class Message {
    protected final UUID context;
    protected final UUID id;

    @Setter
    protected String from;
    @Setter
    protected String to;
    @Setter
    protected MessageType type = MessageType.TEXT;
    @Setter
    protected Object content;
    @Setter
    protected String text;
    protected PassedInterval expire = null;
    @Setter
    protected List<File> attachments = new ArrayList<>();
    @Setter
    protected List<String> urls = new ArrayList<>();

    public Message(UUID context) {
        this.context = context;
        this.id = UUID.randomUUID();
    }

    public void setExpire(long expire) {
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public void addAttachment(File file) {
        attachments.add(file);
    }

    public void addUrl(String url) {
        urls.add(url);
    }

    public void delete() {}
}
