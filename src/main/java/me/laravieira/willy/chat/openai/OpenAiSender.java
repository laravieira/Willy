package me.laravieira.willy.chat.openai;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OpenAiSender implements SenderInterface {
    private final UUID context;

    public OpenAiSender(UUID context) {
        this.context = context;
    }

    public String buildConversation(@NotNull List<UUID> messages) {
        StringBuilder conversation = new StringBuilder();
        for(int i = 0; i < 5 && i < messages.size(); i++) {
            Message message = MessageStorage.of(messages.get(i));
            conversation.append(message.getFrom());
            conversation.append(": ");
            conversation.append(message.getText());
            conversation.append("\r\n");
        }
        return conversation.toString();
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        OpenAiHeader headerBuilder = new OpenAiHeader(context);
        List<UUID> messages = ContextStorage.of(context).getMessages();
        List<String> stopList = new ArrayList<>();
        stopList.add(ContextStorage.of(context).getLastMessage().getFrom());
        String prompt = headerBuilder.build()+buildConversation(messages);

        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .bestOf(OpenAi.BEST_OF)
                .maxTokens(prompt.length()/4 + OpenAi.MAX_TOKENS)
                .frequencyPenalty(OpenAi.FREQUENCY_PENALTY)
                .presencePenalty(OpenAi.PRESENCE_PENALTY)
                .temperature(OpenAi.TEMPERATURE)
                .topP(OpenAi.TOP_P)
                .echo(OpenAi.ECHO)
                .stop(stopList)
                .build();
        CompletionResult result = OpenAi.getService().createCompletion(OpenAi.ENGINE, completionRequest);
        Willy.getLogger().info(prompt);
        Willy.getLogger().info(result.getChoices().get(0).getText());
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
