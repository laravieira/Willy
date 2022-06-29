package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.watson.WatsonSender;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;

import java.util.List;
import java.util.UUID;

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
        UUID id = UUID.nameUUIDFromBytes(("telegram-"+msg.chat().id()).getBytes());

        TelegramSender sender = new TelegramSender(id, msg.chat(), PassedInterval.DISABLE);
        ContextStorage.of(id).setSender(sender);

        TelegramMessage message = new TelegramMessage(id, msg, PassedInterval.DISABLE);
        MessageStorage.add(message);

        new WatsonSender(id).sendText(message.getText());
        Willy.getLogger().getConsole().config(id + ": " + msg);
    }
}
