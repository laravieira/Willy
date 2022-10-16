package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.storage.ContextStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class CommandContext implements CommandListener {
    public static final String COMMAND = "context";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count > 1) {
            if(args[1].equalsIgnoreCase("clear")) {
                List<UUID> contexts = new ArrayList<>(ContextStorage.all().keySet());
                contexts.forEach(ContextStorage::remove);
            }
        }else {
            console.info("-------------------------- Contexts ---------------------------");
            ContextStorage.all().forEach((identifier, context) -> console.info("Context " + identifier + " expire in " + (context.getExpire().remaining() / 1000) + "s."));
            console.info("---------------------------------------------------------------");
        }
    }
}
