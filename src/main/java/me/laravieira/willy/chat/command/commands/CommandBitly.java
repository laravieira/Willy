package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.feature.bitly.Bitly;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandBitly implements CommandListener {
    public static final String COMMAND = "bitly";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(Bitly.canUse) {
            if(count > 1 && args[1].length() > 10
                    && args[1].startsWith("http") && args[1].contains(".") && args[1].contains("/")) {
                String shortLink = new Bitly(args[1]).getShort();
                if(shortLink == null || shortLink.equals(args[1]) || shortLink.isEmpty())
                    console.info("Can't short link, maybe it's already short enouth.");
                else console.info("Smallest link: "+shortLink);
            }else console.info("This entered argument it's not a valid link.");
        }else console.info("Bitly is not enabled, check config file.");
    }
}
