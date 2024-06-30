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

        String name = message.senderJid().user();

        this.content = message;
        this.text = text;
        this.from = name.substring(0, name.contains(" ") ? name.indexOf(" ") : name.length()-1);
        this.to = Willy.getWilly().getName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
