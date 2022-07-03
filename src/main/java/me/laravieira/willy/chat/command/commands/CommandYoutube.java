package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.feature.youtube.Youtube;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandYoutube implements CommandListener {
    public static final String COMMAND = "yt";

    public void execute(@NotNull Logger console, int count, String[] args) {
        Youtube ytd;
        if(count == 2 && args[1].startsWith("https") && args[1].contains("youtube.com") && args[1].contains("v=")) {
            ytd = new Youtube(args[1]);
            if(ytd.getVideo()) {
                ytd.autoChooseAnyFormat(null);
                console.info(" "+ytd.getDownloadLink());
            }
        }else if(count == 3 && (args[1].equalsIgnoreCase("any")
                || args[1].equalsIgnoreCase("best")
                || args[1].equalsIgnoreCase("good")
                || args[1].equalsIgnoreCase("medium")
                || args[1].equalsIgnoreCase("poor"))
                && args[2].startsWith("https") && args[2].contains("youtube.com") && args[2].contains("v=")) {
            ytd = new Youtube(args[2]);
            if(ytd.getVideo()) {
                ytd.autoChooseAnyFormat(args[1]);
                console.info(" "+ytd.getDownloadLink());
            }
        }else if(count < 2)
            console.info("You have to enter the youtube video link.");
        else
            console.info("This entered argument it's not a valid youtube video link.");
    }
}
