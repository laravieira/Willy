package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TelegramListener implements UpdatesListener {
    @Override
    public int process(@NotNull List<Update> list) {
        for(Update update : list) {
            if(update.message() != null || update.message().text() != null)
                onTextMessageReceived(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void onTextMessageReceived(@NotNull Update update) {
        Message msg = update.message();
        Chat chat = msg.chat();
        UUID id = UUID.nameUUIDFromBytes(("telegram-"+chat.id()).getBytes());

        Thread messageHandler = new Thread(() -> {
            TelegramSender sender = new TelegramSender(chat);
            ContextStorage.of(id).setSender(sender);

            TelegramMessage message = new TelegramMessage(id, msg, chat, PassedInterval.DISABLE);
            MessageStorage.add(message);

            ContextStorage.of(message.getContext()).getWatson().getSender().sendText(message.getText());
        });
        messageHandler.setDaemon(true);
        messageHandler.start();
    }
}
