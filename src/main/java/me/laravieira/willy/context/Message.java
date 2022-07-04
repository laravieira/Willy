package me.laravieira.willy.context;

import me.laravieira.willy.utils.PassedInterval;

import java.io.File;
import java.util.*;

public class Message {
    protected final UUID context;
    protected final UUID id;
    protected String from;
    protected String to;
    protected Object content;
    protected String text;
    protected PassedInterval expire = null;
    protected List<File> attachments = new ArrayList<>();

    public Message(UUID context) {
        this.context = context;
        this.id = UUID.randomUUID();
    }

    public void setExpire(long expire) {
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public void delete() {}

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void addAttachment(File file) {
        attachments.add(file);
    }

    public UUID getId() {
        return id;
    }

    public UUID getContext() {
        return context;
    }

    public PassedInterval getExpire() {
        return expire;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Object getContent() {
        return content;
    }

    public String getText() {
        return text;
    }

    public List<File> getAttachments() {
        return attachments;
    }
}
