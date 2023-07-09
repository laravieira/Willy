package me.laravieira.willy.chat.openai;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.utils.WillyUtils;

import java.io.File;
import java.util.*;

public class OpenAiSender implements SenderInterface {
    private final UUID context;

    public OpenAiSender(UUID context) {
        this.context = context;
    }

    @Override
    public void send(Object message) {

    }

    private CompletionResult sendCompletion(CompletionRequest request) {
        try {
            return OpenAi.getService().createCompletion(request);
        }catch(RuntimeException e) {
            if(!e.getMessage().contains("timed out"))
                e.printStackTrace();
            return OpenAi.getService().createCompletion(request);
        }
    }

    @Override
    public void sendText(String message) {
        OpenAiHeader headerBuilder = new OpenAiHeader(context);

        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        List<String> stopList = new ArrayList<>();
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getFrom() + ": ");
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getTo() + ": ");
        String prompt = headerBuilder.build()+WillyUtils.buildConversation(messages, OpenAi.HISTORY_SIZE);

        CompletionResult result = sendCompletion(CompletionRequest.builder()
                .prompt(prompt)
                .bestOf(OpenAi.BEST_OF)
                .maxTokens(prompt.length()/4 + OpenAi.MAX_TOKENS)
                .frequencyPenalty(OpenAi.FREQUENCY_PENALTY)
                .presencePenalty(OpenAi.PRESENCE_PENALTY)
                .temperature(OpenAi.TEMPERATURE)
                .topP(OpenAi.TOP_P)
                .echo(OpenAi.ECHO)
                .stop(stopList)
                .model("text-davinci-003")
                .build());
        new OpenAiListener().onCompletionResponse(result, context);
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
