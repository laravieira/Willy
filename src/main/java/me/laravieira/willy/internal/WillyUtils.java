package me.laravieira.willy.internal;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WillyUtils {
    public static boolean hasWillyCall(@NotNull String message) {
        if(message.contains(Config.getString("name")))
            return true;
        for(String alias : (List<String>)Config.getList("aliases"))
            if(message.contains(alias))
                return true;
        return false;
    }

}
