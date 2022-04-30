package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import me.laravieira.willy.Willy;

import java.util.List;

public class TelegramListener implements UpdatesListener {
    @Override
    public int process(List<Update> list) {
        for(Update update : list) {
            if(update.message() != null || update.message().text() != null)
                onTextMessageReceived(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void onTextMessageReceived(Update update) {
        Message msg = update.message();
        String id = "telegram-"+msg.chat().id();
        TelegramContext context = TelegramContext.getContext(update, msg.chat(), msg, id);
        Willy.getLogger().getConsole().config(id + ": " + msg);
        context.getWatsonMessager().sendTextMessage(msg.text());
    }
}
