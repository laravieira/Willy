package me.laravieira.willy.context;

import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.chat.watson.WatsonContext;

import java.util.*;

public interface ContextInterface {
    // Message storage
    LinkedList<UUID> getMessages();
    Message getMessage(UUID id);
    Message getLastMessage();
    @SuppressWarnings("unused")
    boolean hasMessage(UUID id);
    void addMessage(UUID id);
    void removeMessage(UUID id);

    // User language
    String getLanguage();
    @SuppressWarnings("unused")
    void setLanguage(String language);

    // Context data
    UUID getId();
    PassedInterval getExpire();
    String getApp();
    void setApp(String app);

    // Sender
    SenderInterface getSender();
    void setSender(SenderInterface sender);

    WatsonContext getWatson();
}
