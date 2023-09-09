package me.laravieira.willy.chat.chatgpt;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;

import java.io.File;
import java.util.*;

public class ChatGPTSender implements SenderInterface {
    private final UUID context;

    public ChatGPTSender(UUID context) {
        this.context = context;
    }

    @Override
    public void send(Object message) {

    }

    public static class Time {
        @JsonPropertyDescription("City and state, for example: León, Chacara")
        public String location;
    }

    public static class Color {
        @JsonPropertyDescription("The color, like: Purple, Blue, Red, etc.")
        public String color;
    }

    public static class ColorResponse {
        public String color;

        public ColorResponse(String color) {
            this.color = color;
        }
    }

    @Override
    public void sendText(String message) {
        List<ChatFunction> functions = new ArrayList<>();
        functions.add(ChatFunction.builder()
                .name("get_current_time")
                .description("Get the current time of any location.")
                .executor(Time.class, w -> "12 PM")
                .build());
        functions.add(ChatFunction.builder()
                .name("get_color")
                .description("Get the color.")
                .executor(Color.class, w -> new ColorResponse("Purple"))
                .build());
        FunctionExecutor executor = new FunctionExecutor(functions);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "Oi!!! :)", Willy.getWilly().getName()));
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "Eu sou o Willy, um cãozinho virtual falante da Lara Vieira. Eu sou muito fofo.", Willy.getWilly().getName()));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), message));

        ChatCompletionRequest request = ChatCompletionRequest
                .builder()
                .model(ChatGPT.CHAT_GPT_ENGINE)
                .messages(messages)
                .functions(executor.getFunctions())
                .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                .n(1)
                .maxTokens(150)
                .logitBias(new HashMap<>())
                .build();

        ChatMessage response = ChatGPT.getService().createChatCompletion(request).getChoices().get(0).getMessage();
        messages.add(response);

        ChatFunctionCall functionCall = response.getFunctionCall();

        if(functionCall != null) {
            Optional<ChatMessage> responseMessage = executor.executeAndConvertToMessageSafely(functionCall);
            if(responseMessage.isPresent()) {
                messages.add(responseMessage.get());

                ChatCompletionRequest request2 = ChatCompletionRequest
                        .builder()
                        .model(ChatGPT.CHAT_GPT_ENGINE)
                        .messages(messages)
                        .functions(executor.getFunctions())
                        .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                        .n(1)
                        .maxTokens(150)
                        .logitBias(new HashMap<>())
                        .build();

                ChatMessage response2 = ChatGPT.getService().createChatCompletion(request2).getChoices().get(0).getMessage();
                messages.add(response2);
                new ChatGPTListener().onCompletionResponse(response2.getContent(), context);
            }
        }else {
            new ChatGPTListener().onCompletionResponse(response.getContent(), context);
        }

    }

    @Override
    public void sendLink(Message message) {

    }

    @Override
    public void sendStick(Message message) {

    }

    @Override
    public void sendGif(Message message) {

    }

    @Override
    public void sendImage(Message message) {

    }

    @Override
    public void sendVideo(Message message) {

    }

    @Override
    public void sendAudio(Message message) {

    }

    @Override
    public void sendLocation(Message message) {

    }

    @Override
    public void sendContact(Message message) {

    }

    @Override
    public void sendFile(File message) {

    }
}
