package me.laravieira.willy.context;

import me.laravieira.willy.chat.watson.WatsonSender;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.chat.watson.WatsonContext;

import java.util.*;

public interface ContextInterface {
    // Message storage
    List<UUID> getMessages();
    Message getMessage(UUID id);
    Message getLastMessage();
    boolean hasMessage(UUID id);
    void addMessage(UUID id);
    void removeMessage(UUID id);

    // User language
    String getLanguage();
    void setLanguage(String language);

    // Context data
    UUID getId();
    PassedInterval getExpire();

    // Sender
    SenderInterface getSender();
    void setSender(SenderInterface sender);

    WatsonContext getWatson();
}
