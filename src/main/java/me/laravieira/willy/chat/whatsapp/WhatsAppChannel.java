package me.laravieira.willy.chat.whatsapp;

import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyChannel;
import me.laravieira.willy.WillyMessage;

public class WhatsAppChannel implements WillyChannel {
    private Context context;
    private final String chat;

    WhatsAppChannel(String chat) {
        this.chat = chat;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void send(WillyMessage message) {
        JSONObject body = new JSONObject();
        body.put("phone", chat);
        body.put("message", message.getText());
        WhatsApp.setTyping(chat, false);
        JSONObject response = WhatsApp.post("/send/message", body);
        Willy.getLogger().fine("WhatsAppChannel send message: " + response.toString());
    }

    @Override
    public void sendLast() {
        send(context.getMessages().getLast());
    }
}
