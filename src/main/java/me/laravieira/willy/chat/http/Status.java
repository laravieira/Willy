package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import me.laravieira.willy.internal.Config;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class Status extends Controller {
    public Status(Request request, Response response, Callback callback) {
        super(request, response, callback);
    }

    private boolean generalStatus() {
        if(!Config.getBoolean("openai.enable") || !new OpenAi().isConnected())
            return false;
        if(!Config.getBoolean("discord.enable") || !new Discord().isConnected())
            return false;
        if(!Config.getBoolean("whatsapp.enable") || !new Whatsapp().isConnected())
            return false;
        if(!Config.getBoolean("telegram.enable") || !new Telegram().isConnected())
            return false;
        if(!Config.getBoolean("http-api.enable") || !new HTTP().isConnected())
            return false;
        return true;
    }

    @Override
    public boolean onGet() {
        JSONObject body = new JSONObject() {{
            put("name", Willy.getWilly().getName());
            put("version", Willy.getWilly().getFullVersion());
            put("status", generalStatus());
            put("environment", Config.getString("environment"));
            put("openai", new JSONObject() {{
                put("enabled", Config.getBoolean("openai.enable"));
                put("connected", new OpenAi().isConnected());
            }});
            put("discord", new JSONObject() {{
                put("enabled", Config.getBoolean("discord.enable"));
                put("connected", new Discord().isConnected());
            }});
            put("whatsapp", new JSONObject() {{
                put("enabled", Config.getBoolean("whatsapp.enable"));
                put("connected", new Whatsapp().isConnected());
            }});
            put("telegram", new JSONObject() {{
                put("enabled", Config.getBoolean("telegram.enable"));
                put("connected", new Telegram().isConnected());
            }});
            put("http-api", new JSONObject() {{
                put("enabled", Config.getBoolean("http-api.enable"));
                put("port", Config.getInt("http-api.port"));
                put("connected", new HTTP().isConnected());
            }});
        }};
        return toJSON(body);
    }
}
