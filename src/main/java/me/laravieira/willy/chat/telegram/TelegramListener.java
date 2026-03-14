package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import me.laravieira.willy.Context;
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
            TelegramChannel sender = new TelegramChannel(chat);
            Context context = Context.of(id, sender, "Telegram", chat.username());

            TelegramMessage message = new TelegramMessage(msg, chat, PassedInterval.DISABLE);
            if(msg.photo() != null && msg.photo().length > 0) {
                for(PhotoSize photo : msg.photo()) {
                    GetFileResponse response = Telegram.getBot().execute(new GetFile(photo.fileId()));
                    if(response.isOk())
                        message.addFile(Telegram.getBot().getFullFilePath(response.file()));
                }
            }
            context.process(message);
        });
        messageHandler.setDaemon(true);
        messageHandler.start();
    }
}
