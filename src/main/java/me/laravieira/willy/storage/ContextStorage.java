package me.laravieira.willy.storage;

import me.laravieira.willy.context.Context;

import java.util.*;

public class ContextStorage {
    private static final Map<UUID, Context> contexts = new HashMap<>();

    public static Context of(UUID id) {
        contexts.putIfAbsent(id, new Context(id));
        return contexts.get(id);
    }

    public static boolean has(UUID id) {
        return contexts.containsKey(id);
    }

    public static void refresh() {
        try {
            List<UUID> delete = new ArrayList<>();
            contexts.forEach((id, context) -> {
                if (context.getExpire().hasPassedInterval())
                    delete.add(id);
            });
            delete.forEach(id -> {
                Context context = contexts.get(id);
                contexts.remove(id);
                context.getMessages().forEach(messageId -> {
                    if (!MessageStorage.of(messageId).getExpire().isEnable())
                        MessageStorage.remove(messageId);
                });
            });
        }catch (RuntimeException ignored) {}
    }

    public static int size() {
        return contexts.size();
    }

    public static Map<UUID, Context> all() {
        return contexts;
    }
}
