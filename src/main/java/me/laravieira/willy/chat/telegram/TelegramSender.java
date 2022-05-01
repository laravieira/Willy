package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.request.SendMessage;
import me.laravieira.willy.kernel.Sender;

public class TelegramSender extends Sender {
    private final TelegramContext context;

    public TelegramSender(TelegramContext context) {
        super(context);
        this.context = context;
    }

    public void send(String message) {
        SendMessage send = new SendMessage(context.getChat().id(), message);
        Telegram.getBot().execute(send);
    }

}
