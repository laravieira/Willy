package me.laravieira.willy.internal;

import me.laravieira.willy.Willy;

import java.util.List;

public class WillyUtils {
    public static boolean hasWillyCall(String message) {
        if(message.contains(Willy.getConfig().asString("willy-name")))
            return true;
        for(String alias : (List<String>)Willy.getConfig().asList("willy-aliases"))
            if(message.contains(alias))
                return true;
        return false;
    }

}