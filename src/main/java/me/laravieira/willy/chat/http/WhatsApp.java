package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.chat.whatsapp.WhatsAppListener;
import me.laravieira.willy.chat.whatsapp.WhatsAppMessage;
import me.laravieira.willy.internal.Config;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class WhatsApp extends Controller {
    public WhatsApp(Request request, Response response, Callback callback) {
        super(request, response, callback);
    }

    @Override
    public boolean onPost() {
        JSONObject response = new JSONObject();
        if(!Config.getBoolean("whatsapp.enable")) {
            response.put("status", "DISABLED");
            return toJSON(response, 400);
        }

        String raw;
        try {
            raw = Content.Source.asString(request, StandardCharsets.UTF_8);
        }catch (Exception e) {
            response.put("status", "ERROR");
            return toJSON(response, 400);
        }


        String secret = Config.getString("whatsapp.secret");
        if(secret != null && !secret.isEmpty()) {
            String signature = request.getHeaders().get("x-hub-signature-256").replace("sha256=", "");
            if (!verify(secret, raw, signature)) {
                response.put("status", "UNAUTHORIZED");
                return toJSON(response, 403);
            }
        }

        JSONObject json = JSON.parseObject(raw);
        if (!json.containsKey("device_id") || !json.getString("device_id").equals(Config.getLong("whatsapp.phone_number")+"@s.whatsapp.net")) {
            response.put("status", "IGNORED");
            return toJSON(response, 200);
        }
        Runnable callback = () -> {};
        response.put("status", "OK");
        switch (json.getString("event")) {
            case "message" -> callback = () -> WhatsAppListener.onMessage(new WhatsAppMessage(json.getJSONObject("payload")));
            case "message.ack" -> callback = () -> WhatsAppListener.onMessageAck(json.getJSONObject("payload"));
            case "message.reaction" -> callback = () -> WhatsAppListener.onMessageReaction(json.getJSONObject("payload"));
            case "message.revoked" -> callback = () -> WhatsAppListener.onMessageRevoked(json.getJSONObject("payload"));
            case "message.edited" -> callback = () -> WhatsAppListener.onMessageEdited(json.getJSONObject("payload"));
            case "message.deleted" -> callback = () -> WhatsAppListener.onMessageDeleted(json.getJSONObject("payload"));
            default -> response.put("status", "INVALID");
        }
        Thread thread = new Thread(callback);
        thread.setDaemon(true);
        thread.start();

        return toJSON(response, 200);
    }

    private static boolean verify(String secret, String requestBody, String providedSignature) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);

            byte[] rawHash = mac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(rawHash);

            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
