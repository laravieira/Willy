package me.laravieira.willy.storage;

import me.laravieira.willy.context.Message;

import java.util.*;

public class MessageStorage {
    private static final Map<UUID, Message> messages = new HashMap<>();

    public static Map<UUID, Message> all() {
        return messages;
    }

    public static Message of(UUID id) {
        return messages.get(id);
    }

    public static boolean has(UUID id) {
        return messages.containsKey(id);
    }

    public static UUID add(Message message) {
        if(ContextStorage.has(message.getContext())) {
            ContextStorage.of(message.getContext()).addMessage(message.getId());
        }
        messages.put(message.getId(), message);
        return message.getId();
    }

    public static void remove(UUID id) {
        UUID context = messages.get(id).getContext();
        if(ContextStorage.has(context))
            ContextStorage.of(context).removeMessage(id);
        messages.get(id).delete();
        messages.remove(id);
    }

    public static void refresh() {
        try{
            List<UUID> delete = new ArrayList<>();
            messages.forEach((id, message) -> {
                if(message.getExpire().hasPassedInterval())
                    delete.add(id);
            });
            delete.forEach(MessageStorage::remove);
        }catch (RuntimeException ignored) {}
    }

    public static int size() {
        return messages.size();
    }
}
