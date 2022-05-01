package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import me.laravieira.willy.kernel.Context;

public class TelegramContext extends Context{
    private Update update;
    private Chat chat;
    private Message message;
    private final TelegramSender sender;

    public static TelegramContext getContext(Update update, Chat chat, Message message, String id) {
        TelegramContext context;
        if(Context.getContexts().containsKey(id)) {
            context = (TelegramContext) Context.getContexts().get(id);
            context.setUpdate(update);
            context.setChat(chat);
            context.setMessage(message);
        }else {
            context = new TelegramContext(update, chat, message, id);
            Context.getContexts().put(id, context);
        }
        return context;
    }

    public TelegramContext(Update update, Chat chat, Message message, String id) {
        super(id);
        this.update = update;
        this.chat = chat;
        this.message = message;
        this.sender = new TelegramSender(this);
    }


    public void setUpdate(Update update) {
        this.update = update;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setMessage(Message message) {
        this.message = message;
    }


    public Update getUpdate() {
        return update;
    }

    public Chat getChat() {
        return chat;
    }

    public Message getMessage() {
        return message;
    }

    public TelegramSender getSender() {
        return sender;
    }
}
