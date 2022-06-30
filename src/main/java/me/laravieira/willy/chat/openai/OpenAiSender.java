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
import java.util.*;

public class OpenAiSender implements SenderInterface {
    private final UUID context;

    public OpenAiSender(UUID context) {
        this.context = context;
    }

    public String buildConversation(@NotNull LinkedList<UUID> messages) {
        StringBuilder conversation = new StringBuilder();
        LinkedList<UUID> lastMessages = new LinkedList<>(messages);
        LinkedList<Message> descendingHistory = new LinkedList<>();
        for(int i = 0; i < lastMessages.size() && i < OpenAi.HISTORY_SIZE; i++)
            descendingHistory.add(MessageStorage.of(lastMessages.pollLast()));
        Iterator<Message> history = descendingHistory.descendingIterator();
        while(history.hasNext()) {
            Message message = history.next();
            conversation.append(message.getFrom());
            conversation.append(": ");
            conversation.append(message.getText());
            conversation.append("\r\n");
        }
        conversation.append(Willy.getWilly().getName());
        conversation.append(": ");
        return conversation.toString();
    }

    @Override
    public void send(Object message) {

    }

    private CompletionResult sendCompletion(CompletionRequest request) {
        try {
            return OpenAi.getService().createCompletion(OpenAi.ENGINE, request);
        }catch(RuntimeException e) {
            if(!e.getMessage().contains("timed out"))
                e.printStackTrace();
            return OpenAi.getService().createCompletion(OpenAi.ENGINE, request);
        }
    }

    @Override
    public void sendText(String message) {
        OpenAiHeader headerBuilder = new OpenAiHeader(context);
        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        List<String> stopList = new ArrayList<>();
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getFrom() + ": ");
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getTo() + ": ");
        String prompt = headerBuilder.build()+buildConversation(messages);

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
