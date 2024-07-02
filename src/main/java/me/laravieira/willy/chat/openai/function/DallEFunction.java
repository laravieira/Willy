package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.image.Image;
import io.github.sashirestela.openai.domain.image.ImageRequest;
import io.github.sashirestela.openai.domain.image.ImageResponseFormat;
import io.github.sashirestela.openai.domain.image.Size;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.context.Context;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.MessageType;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;

import java.util.List;

public class DallEFunction extends Functional {
    @JsonPropertyDescription("Prompt to generate the image.")
    @JsonProperty(required = true)
    public String prompt;


    public static FunctionDef builder() {
        return FunctionDef.builder()
            .name("dall-e")
            .description("Generate an image with the given prompt.")
            .functionalClass(DallEFunction.class)
            .build();
    }

    @Override
    public Object execute() {
        try {
            if(!Config.getBoolean("openai.dall_e")) {
                throw new Exception("Dall-E is not enabled.");
            }
            if(this.context == null) {
                throw new Exception("Context is null.");
            }
            if(prompt == null || prompt.isEmpty()) {
                throw new Exception("Prompt is empty.");
            }

            ImageRequest.Style style = ImageRequest.Style.NATURAL;
            Size size = Size.X256;
            int amount = 1;

            ImageRequest request = ImageRequest.builder()
                    .model("dall-e-2")
                    .prompt(prompt)
                    .style(style)
                    .size(size)
                    .n(amount)
                    .responseFormat(ImageResponseFormat.URL)
                    .build();
            List<Image> images = OpenAi.getService().images().create(request).get();

            if(images == null || images.isEmpty()) {
                throw new Exception("An error occurred while generating the image.");
            }
            Context context = ContextStorage.of(this.context);
            List<String> urls = images.stream().map(Image::getUrl).toList();

            ChatMessage.ToolMessage toolMessage = ChatMessage.ToolMessage.of("Image generated and sent.", this.call);
            Message result = new Message(context.getId());
            result.setExpire(PassedInterval.DISABLE);
            result.setTo(MessageStorage.of(context.getMessages().getFirst()).getFrom());
            result.setFrom("SYSTEM");
            result.setContent(toolMessage);
            result.setUrls(urls);
            result.setType(MessageType.IMAGE);
            MessageStorage.add(result);

            Willy.getLogger().fine(STR."Dall-E function \{result.getId()}");
            context.getUserSender().send(result);
            context.getSender().sendText(result.toString());
            return toolMessage.getContent();
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Dal-E function error: \{e.getMessage()}");
            return askResponse(STR."Something didn't work right: \{e.getMessage()}");
        }
    }
}
