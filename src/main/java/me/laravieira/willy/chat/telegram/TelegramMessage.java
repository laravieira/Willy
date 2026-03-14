package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

public class TelegramMessage extends WillyMessage {
    public TelegramMessage(@NotNull com.pengrad.telegrambot.model.Message message, @NotNull Chat chat, long expire) {
        super(message);
        this.content = message;
        this.text = message.text();
        this.from = chat.firstName();
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }
}
