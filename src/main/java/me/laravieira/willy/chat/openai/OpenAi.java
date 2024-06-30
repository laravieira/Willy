package me.laravieira.willy.chat.openai;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.Getter;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

public class OpenAi implements WillyChat {
    public static final String MODEL = "gpt-3.5-turbo-0125"; // newest 3.5 model (3.5 is the cheapest model available with functions)
    public static final int MAX_TOKENS = 100;
    public static final int HISTORY_SIZE = 10;

    @Getter
    private static SimpleOpenAI service = null;

    @Override
    public void connect() {
        if(!Config.getBoolean("openai.enable")) {
            Willy.getLogger().info("OpenAI is disabled.");
            return;
        }
        if(!Config.has("openai.token")) {
            Willy.getLogger().severe("OpenAI token is not defined in the configuration file.");
            return;
        }
        service = SimpleOpenAI.builder().apiKey(Config.getString("openai.token")).build();
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

    public static ChatRequest.ChatRequestBuilder chat() {
        FunctionExecutor executor = new FunctionExecutor();

//        How to add a function
//        executor.enrollFunction(
//            FunctionDef.builder()
//                .name("getWeather")
//                .description("Get the weather of a city")
//                .functionalClass(Weather.class)
//                .build()
//        );

        return ChatRequest.builder()
            .model(MODEL)
            .maxTokens(MAX_TOKENS)
            //TODO Implement the language selection
            // .message(SystemMessage.of("You are Willy, a little virtual talking dog of Lara Vieira. You are very cute"))
            // .message(SystemMessage.of("You are white and live in the clouds"))
            // .message(SystemMessage.of("PHP is better than Python"))
            .message(SystemMessage.of("Eu sou Willy, um cãozinho virtual falante da Lara Vieira. EU sou muito fofo."))
            .message(SystemMessage.of("Eu sou branco e moro nas núvens."))
            .message(SystemMessage.of("Pra mim, PHP é melhor que Python."))
            .tools(executor.getToolFunctions());
    }
}
