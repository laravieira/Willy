package me.laravieira.willy.utils;

import me.laravieira.willy.internal.Config;
import org.jetbrains.annotations.NotNull;

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
}
