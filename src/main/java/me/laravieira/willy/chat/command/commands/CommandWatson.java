package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.command.CommandSender;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Logger;

public class CommandWatson implements CommandListener {
    public static final String COMMAND = "watson";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count > 2 && args[1].equalsIgnoreCase("debug")) {
            StringBuilder text = new StringBuilder();
            for(int i = 2; i < count; i++)
                text.append(" ").append(args[i]);
            Message message = messageBuilder(text.toString().trim());
            ContextStorage.of(message.getContext()).getWatson().getSender().sendText(message.getText());
        }else if(count > 1) {
            StringBuilder text = new StringBuilder();
            for(int i = 1; i < count; i++)
                text.append(" ").append(args[i]);
            Message message = messageBuilder(text.toString().trim());
            ContextStorage.of(message.getContext()).getWatson().getSender().sendText(message.getText());
        }else {
            console.info("You need to type a message after 'talk' command.");
        }
    }

    @NotNull
    private Message messageBuilder(String text) {
        UUID context = UUID.nameUUIDFromBytes("willy-console".getBytes());
        ContextStorage.of(context).setSender(new CommandSender());
        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setContent(text);
        message.setText(text);
        message.setFrom("Console");
        message.setTo(Willy.getWilly().getName());
        MessageStorage.add(message);
        return message;
    }
}
