package me.laravieira.willy.command;

import me.laravieira.willy.Willy;
import me.laravieira.willy.kernel.Context;

import java.util.logging.Logger;

public class CommandContext extends Context{
    private Logger logger;
    private final CommandSender sender;

    public static CommandContext getContext() {
        if(!Context.getContexts().containsKey("willy-command")) {
            CommandContext context = new CommandContext(Willy.getLogger().getConsole());
            Context.getContexts().put("willy-command", context);
        }
        return (CommandContext) Context.getContexts().get("willy-command");
    }

    public CommandContext(Logger logger) {
        super("willy-command");
        this.logger = logger;
        this.sender = new CommandSender(this);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public CommandSender getSender() {
        return sender;
    }
}
