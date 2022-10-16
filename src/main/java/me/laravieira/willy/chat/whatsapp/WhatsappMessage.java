package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.model.info.MessageInfo;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WhatsappMessage extends Message {

    public WhatsappMessage(UUID context, @NotNull MessageInfo message, String text, long expire) {
        super(context);
        this.content = message;
        this.text = text;
        this.from = message.senderName().substring(0, message.senderName().contains(" ") ? message.senderName().indexOf(" ") : message.senderName().length()-1);
        this.to = Willy.getWilly().getName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
