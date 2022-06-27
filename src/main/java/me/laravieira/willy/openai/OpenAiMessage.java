package me.laravieira.willy.openai;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import me.laravieira.willy.kernel.Context;

public class OpenAiMessage {
    public void sendMessage(String message, String user) {
        sendTextMessage(message, Context.getContext("console"));
    }

    public void sendTextMessage(String message, Context context) {
        OpenAiHeader headerBuilder = new OpenAiHeader(context);
        String header = headerBuilder.build();
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(header+context.getId()+": "+message+"\r\n")
                .bestOf(OpenAi.BEST_OF)
                .maxTokens(OpenAi.MAX_TOKENS)
                .frequencyPenalty(OpenAi.FREQUENCY_PENALTY)
                .presencePenalty(OpenAi.PRESENCE_PENALTY)
                .temperature(OpenAi.TEMPERATURE)
                .topP(OpenAi.TOP_P)
                .echo(OpenAi.ECHO)
                .build();
        CompletionResult result = OpenAi.getService().createCompletion(OpenAi.ENGINE, completionRequest);
        new OpenAiListener().onCompletionResponse(result);
    }
}
