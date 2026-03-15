package me.laravieira.willy.chat.whatsapp;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

import java.time.Instant;
import java.util.Date;

@Getter
public class WhatsAppMessage {
    private final String body;
    private final String chatId;
    private final String chatLId;
    private final String from;
    private final String fromLId;
    private final String fromName;
    private final String id;
    private final boolean fromMe;
    private final Date date;

    public WhatsAppMessage(JSONObject object) {
        id = object.getString("id");
        body = object.getString("body");
        chatId = object.getString("chat_id");
        chatLId = object.getString("chat_lid");
        from = object.getString("from");
        fromLId = object.getString("from_lid");
        fromName = object.getString("from_name");
        fromMe = object.getBoolean("is_from_me");
        date = Date.from(Instant.parse(object.getString("timestamp")));
    }
}
