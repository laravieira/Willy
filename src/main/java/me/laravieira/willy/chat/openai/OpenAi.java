package me.laravieira.willy.chat.openai;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.Getter;
import me.laravieira.willy.*;
import me.laravieira.willy.chat.openai.function.BitlyFunction;
import me.laravieira.willy.chat.openai.function.DallEFunction;
import me.laravieira.willy.internal.Config;

import java.util.UUID;

public class OpenAi implements WillyBrain {
    public static final String MODEL = "gpt-3.5-turbo-0125"; // newest 3.5 model (3.5 is the cheapest model available with functions)
    public static final int MAX_TOKENS = 100;
    public static final int HISTORY_SIZE = 10;

    @Getter
    private static SimpleOpenAI service = null;

    @Getter
    private static final FunctionExecutor executor = new FunctionExecutor();

    @Override
    public void connect() {
        if(!Config.getBoolean("openai.enable")) {
            Willy.getLogger().info("OpenAI service is disabled.");
            return;
        }
        if(!Config.has("openai.token")) {
            Willy.getLogger().severe("OpenAI token is not defined in the configuration file.");
            return;
        }
        if(!Config.has("openai.prompt") || Config.getFile("openai.prompt") == null) {
            Willy.getLogger().severe("OpenAI prompt is not defined in the specified path.");
            return;
        }
        service = SimpleOpenAI.builder().apiKey(Config.getString("openai.token")).build();
        Willy.getLogger().info("OpenAI service connected successfully.");
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
    public void refresh() {}

    @Override
    public String getName() {
        return "willy";
    }

    public static ChatRequest.ChatRequestBuilder chat() {
        executor.enrollFunction(BitlyFunction.builder());
        executor.enrollFunction(DallEFunction.builder());

        return ChatRequest.builder()
            .model(MODEL)
            .maxTokens(MAX_TOKENS)
            .message(SystemMessage.of(Config.getFileContent("openai.prompt")))
            .tools(executor.getToolFunctions());
    }

    @Override
    public WillyChannel getChannel(Context context) {
        OpenAiSender channel = new OpenAiSender();
        channel.setContext(context);
        return channel;
    }
}
