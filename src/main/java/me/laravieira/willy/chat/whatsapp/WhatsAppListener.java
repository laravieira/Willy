package me.laravieira.willy.chat.whatsapp;

import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;

import java.util.UUID;

public class WhatsAppListener {
    public static void onMessage(WhatsAppMessage event) {
        Willy.getLogger().info("Received WhatsApp message: " + event.getBody());
        if(event.isFromMe())
            return;

        UUID id = UUID.nameUUIDFromBytes(event.getChatId().getBytes());
        Context context = Context.of(id, new WhatsAppChannel(event.getChatId()), "Whatsapp", event.getFromName());
        WillyMessage message = new WillyMessage(event.getBody());
        message.setText(event.getBody());
        context.process(message);
        WhatsApp.setRead(event.getChatId(), event.getId());
        WhatsApp.setTyping(event.getChatId(), true);
    }

    public static void onMessageAck(JSONObject json) {
        Willy.getLogger().info("Received WhatsApp message.ack: " + json.toJSONString());
    }

    public static void onMessageReaction(JSONObject json) {
        Willy.getLogger().info("Received WhatsApp message.reaction: " + json.toJSONString());
    }

    public static void onMessageRevoked(JSONObject json) {
        Willy.getLogger().info("Received WhatsApp message.revoked: " + json.toJSONString());
    }

    public static void onMessageEdited(JSONObject json) {
        Willy.getLogger().info("Received WhatsApp message.edited: " + json.toJSONString());
    }

    public static void onMessageDeleted(JSONObject json) {
        Willy.getLogger().info("Received WhatsApp message.deleted: " + json.toJSONString());
    }
}
