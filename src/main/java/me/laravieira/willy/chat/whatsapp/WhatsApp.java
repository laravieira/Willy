package me.laravieira.willy.chat.whatsapp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyChat;
import me.laravieira.willy.internal.Config;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class WhatsApp implements WillyChat {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpRequest.Builder builder;
    private static String host;

    @Override
    public void connect() {
        // Listener is handled by me.laravieira.willy.http.WhatsApp (webhook)
        host = Config.getString("whatsapp.gowa.host");
        String device = Config.getString("whatsapp.gowa.device");
        String user = Config.getString("whatsapp.gowa.user");
        String password = Config.getString("whatsapp.gowa.password");
        String token = Base64.getEncoder().encodeToString((user + ':' + password).getBytes(StandardCharsets.UTF_8));

        builder = HttpRequest.newBuilder()
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Authorization", "Basic " + token)
            .header("X-Device-Id", device);

        JSONObject presence = new JSONObject();
        presence.put("type", "available");
        post("/send/presence", presence);
    }

    @Override
    public void disconnect() {
        JSONObject presence = new JSONObject();
        presence.put("type", "unavailable");
        post("/send/presence", presence);
    }

    @Override
    public boolean isConnected() {
        JSONObject response = get("/app/status");
        return response.getString("code").equals("SUCCESS");
    }

    @Override
    public void refresh() {}

    @Override
    public String getName() { return "WhatsApp"; }

    public static void setTyping(@NotNull String chat, boolean typing) {
        JSONObject body = new JSONObject();
        body.put("phone", chat);
        body.put("action", typing ? "start" : "stop");
        http("/send/chat-presence", body);
    }

    public static void setRead(@NotNull String chat, @NotNull String id) {
        JSONObject body = new JSONObject();
        body.put("phone", chat);
        http("/message/"+id+"/read", body);
    }

    public static JSONObject get(@NotNull String path) {
        return http(path, null);
    }

    public static JSONObject post(@NotNull String path, @NotNull JSONObject object) {
        return http(path, object);
    }

    private static JSONObject http(@NotNull String path, JSONObject body) {
        try {
            HttpRequest.Builder builder = WhatsApp.builder.copy().uri(URI.create(host+path));

            if(body != null)
                builder.POST(HttpRequest.BodyPublishers.ofString(body.toString()));
            else
                builder.GET();

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() < 300 &&  response.statusCode() > 199)
                return JSON.parseObject(response.body());
        } catch (Exception e) {
            Willy.getLogger().severe("Failed to send WhatsApp message: " + e.getMessage());
        }
        return null;
    }
}
