package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TelegramMessage extends Message {

    public TelegramMessage(UUID context, @NotNull com.pengrad.telegrambot.model.Message message, @NotNull Chat chat, long expire) {
        super(context);
        this.content = message;
        this.text = message.text();
        this.from = chat.firstName();
        this.to = Willy.getWilly().getName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
