package me.laravieira.willy.chat.command;

import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;

public interface CommandListener {
    void execute(@NotNull Logger console, int count, String[] args);
}
