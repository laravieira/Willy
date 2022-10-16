package me.laravieira.willy.context;

import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.chat.watson.WatsonContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Context implements ContextInterface {
    private final PassedInterval expire = new PassedInterval(Config.getLong("context_lifetime"));
    private final UUID id;
    private final LinkedList<UUID> history = new LinkedList<>();
    private final WatsonContext watson;
    private SenderInterface sender;
    private String language = "default";
    private String app = "discord";

    public Context(UUID id) {
        this.id = id;
        this.expire.start();
        this.watson = new WatsonContext(id);
    }

    @Override
    public LinkedList<UUID> getMessages() {
        return history;
    }

    @Override
    public Message getMessage(UUID id) {
        return MessageStorage.of(id);
    }

    @Override
    public Message getLastMessage() {
        return MessageStorage.of(history.getLast());
    }

    @Override
    public boolean hasMessage(UUID id) {
        return history.contains(id);
    }

    @Override
    public void addMessage(UUID id) {
        expire.reset();
        history.addLast(id);
    }

    @Override
    public void removeMessage(UUID id) {
        history.remove(id);
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(String language) {
        expire.reset();
        this.language = language;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public PassedInterval getExpire() {
        return expire;
    }

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public WatsonContext getWatson() {
        return watson;
    }

    @Override
    public SenderInterface getSender() {
        return sender;
    }

    @Override
    public void setSender(SenderInterface sender) {
        this.sender = sender;
    }

}
