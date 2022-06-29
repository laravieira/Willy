package me.laravieira.willy.chat.telegram;

import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TelegramMessage extends Message {

    public TelegramMessage(UUID context, @NotNull com.pengrad.telegrambot.model.Message message, long expire) {
        super(context);
        this.content = message;
        this.text = message.text();
        this.from = message.chat().username();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
