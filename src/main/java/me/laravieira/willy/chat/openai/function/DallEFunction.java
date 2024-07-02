package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.opsmatters.bitly.Bitly;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.Functional;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;

public class DallEFunction implements Functional {
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
            if(prompt == null || prompt.isEmpty()) {
                throw new Exception("Prompt is empty.");
            }

            return "Not implemented yet.";
        } catch (Exception e) {
            Willy.getLogger().warning(e.getMessage());
            return "Something didn't work right.";
        }
    }
}
