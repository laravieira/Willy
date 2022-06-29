package me.laravieira.willy.chat.telegram;

import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;

import java.util.UUID;

public class TelegramMessage extends Message {
    private final com.pengrad.telegrambot.model.Message message;

    public TelegramMessage(UUID context, com.pengrad.telegrambot.model.Message message, long expire) {
        super(context);
        this.message = message;
        this.content = message;
        this.text = message.text();
        this.from = message.chat().username();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
