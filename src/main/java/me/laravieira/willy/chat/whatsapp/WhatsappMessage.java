package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.standard.TextMessage;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;

import java.util.UUID;

public class WhatsappMessage extends Message {
    private final MessageInfo message;

    public WhatsappMessage(UUID context, MessageInfo message, long expire) {
        super(context);
        this.message = message;
        this.content = message;
        this.text = ((TextMessage)message.message().content()).text();
        this.from = message.senderName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
