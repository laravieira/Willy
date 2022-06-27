package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.info.MessageInfo;
import me.laravieira.willy.kernel.Context;

public class WhatsappContext extends Context{
    private Chat chat;
    private MessageInfo message;
    private final WhatsappSender sender;

    public static WhatsappContext getContext(Chat chat, MessageInfo message, String id) {
        WhatsappContext context;
        if(Context.getContexts().containsKey(id)) {
            context = (WhatsappContext) Context.getContexts().get(id);
            context.setChat(chat);
            context.setMessage(message);
        }else {
            context = new WhatsappContext(chat, message, id);
            Context.getContexts().put(id, context);
        }
        return context;
    }

    public WhatsappContext(Chat chat, MessageInfo message, String id) {
        super(id);
        this.chat = chat;
        this.message = message;
        this.sender = new WhatsappSender(this);
    }


    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setMessage(MessageInfo message) {
        this.message = message;
    }


    public Chat getChat() {
        return chat;
    }

    public MessageInfo getMessage() {
        return message;
    }

    public WhatsappSender getSender() {
        return sender;
    }
}
