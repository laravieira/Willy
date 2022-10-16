package me.laravieira.willy.utils;

import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.MessageStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public class WillyUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasWillyCall(@NotNull String message) {
        if(message.contains(Config.getString("name")))
            return true;
        for(String alias : Config.getStringList("aliases"))
            if(message.contains(alias))
                return true;
        return false;
    }

    public static String buildConversation(@NotNull LinkedList<UUID> messages, @NotNull int historySize) {
        StringBuilder conversation = new StringBuilder();
        LinkedList<UUID> lastMessages = new LinkedList<>(messages);
        LinkedList<Message> descendingHistory = new LinkedList<>();
        for(int i = 0; i < lastMessages.size() && i < historySize; i++)
            descendingHistory.add(MessageStorage.of(lastMessages.pollLast()));
        Iterator<Message> history = descendingHistory.descendingIterator();
        while(history.hasNext()) {
            Message message = history.next();
            conversation.append(message.getFrom());
            conversation.append(": ");
            conversation.append(message.getText());
            conversation.append("\r\n");
        }
        conversation.append(Willy.getWilly().getName());
        conversation.append(": ");
        return conversation.toString();
    }
}
