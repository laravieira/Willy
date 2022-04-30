package me.laravieira.willy.command;

import me.laravieira.willy.kernel.Sender;

public class CommandSender extends Sender {
    private CommandContext context;

    public CommandSender(CommandContext context) {
        super(context);
        this.context = context;
    }

    public void send(String message) {
        if(context.getDebugWatsonMessage())
            context.getWatsonMessager().debug(context.getLogger());
        else context.getLogger().info(message);
    }

}
