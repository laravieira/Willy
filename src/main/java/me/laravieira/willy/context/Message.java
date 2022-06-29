package me.laravieira.willy.context;

import me.laravieira.willy.utils.PassedInterval;
;
import java.util.*;

public class Message {
    public static final int TEXT = 0;
    public static final int LINK = 1;
    public static final int STICK = 2;
    public static final int GIF = 3;
    public static final int IMAGE = 4;
    public static final int VIDEO = 5;
    public static final int AUDIO = 6;
    public static final int LOCATION = 7;
    public static final int CONTACT = 8;
    public static final int FILE = 9;


    protected final UUID context;
    protected final UUID id;
    protected String from;
    protected String to;
    protected Object content;
    protected String text;
    protected int type = TEXT;
    protected PassedInterval expire = null;

    public Message(UUID context) {
        this.context = context;
        this.id = UUID.randomUUID();
    }

    public void setExpire(long expire) {
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    public void delete() {}

    public void setType(int type) {
        this.type = type;
    }

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

    public UUID getId() {
        return id;
    }

    public UUID getContext() {
        return context;
    }

    public int getType() {
        return type;
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
}
