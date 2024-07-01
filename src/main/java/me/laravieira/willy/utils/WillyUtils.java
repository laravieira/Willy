package me.laravieira.willy.utils;

import io.github.sashirestela.openai.domain.chat.ChatMessage;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.MessageStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WillyUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasWillyName(@NotNull String message, @NotNull List<String> calls) {
        for(String alias : calls)
            if(message.contains(alias))
                return true;
        return false;
    }

    public static boolean startsWith(@NotNull String message, @NotNull List<String> starts) {
        for(String prefix : starts)
            if(message.startsWith(prefix))
                return true;
        return false;
    }


    public static List<ChatMessage> parseContextToOpenAIChat(@NotNull LinkedList<UUID> messages, int historySize) {
        List<ChatMessage> chat = new ArrayList<>();
        LinkedList<UUID> lastMessages = new LinkedList<>(messages);
        LinkedList<Message> descendingHistory = new LinkedList<>();
        for(int i = 0; i < lastMessages.size() && i < historySize; i++)
            descendingHistory.add(MessageStorage.of(lastMessages.pollLast()));
        Iterator<Message> history = descendingHistory.descendingIterator();

        while(history.hasNext()) {
            Message message = history.next();
            if(message.getFrom().equals(Willy.getWilly().getName()))
                chat.add((ChatMessage.ResponseMessage) message.getContent());
            else if(message.getFrom().equals("SYSTEM"))
                chat.add((ChatMessage.ToolMessage) message.getContent());
            else
                chat.add(ChatMessage.UserMessage.of(message.getText()));
        }
        return chat;
    }
}
