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
                console.info("Download link: " + ytd.getDownloadLink());
                ytd.download();
            }
        }else if(count < 2)
            console.info("You have to enter the youtube video link.");
        else
            console.info("This entered argument it's not a valid youtube video link.");
    }
}
