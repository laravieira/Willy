package me.laravieira.willy.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import me.laravieira.willy.Context;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.WillyChannel;

public class CommandChannel implements WillyChannel {
    private final ChatInputInteractionEvent event;
    private Context context;

    public CommandChannel(ChatInputInteractionEvent event) {
        this.event = event;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void send(WillyMessage message) {
        sendText(message.getText());
    }

    @Override
    public void sendLast() {
        send(context.getMessages().getLast());
    }

    public void sendText(String message) {
        event.reply(message);
    }
}
