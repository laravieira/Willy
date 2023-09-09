package me.laravieira.willy.chat.chatgpt;

import com.theokanning.openai.service.OpenAiService;
import lombok.Getter;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

public class ChatGPT implements WillyChat {
    public static final String CHAT_GPT_ENGINE = "gpt-3.5-turbo"; // Ideally "gpt-4" but it's 20~30x more expensive

    @Getter
    private static OpenAiService service = null;

    @Override
    public void connect() {
        if(!Config.getBoolean("chatgpt.enable"))
            return;
        service = new OpenAiService(Config.getString("chatgpt.token"));
    }

    @Override
    public void disconnect() {
        service = null;
    }

    @Override
    public boolean isConnected() {
        return service != null;
    }

    @Override
    public void refresh() {

    }
}
