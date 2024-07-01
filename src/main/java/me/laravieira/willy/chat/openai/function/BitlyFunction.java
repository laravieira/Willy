package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.opsmatters.bitly.Bitly;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.Functional;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;

public class BitlyFunction implements Functional {
    @JsonPropertyDescription("The url to be shortened.")
    @JsonProperty(required = true)
    public String link;

    public static FunctionDef builder() {
        return FunctionDef.builder()
            .name("shortenLink")
            .description("Shorten a link using Bitly")
            .functionalClass(BitlyFunction.class)
            .build();
    }

    @Override
    public Object execute() {
        try {
            if(!Config.getBoolean("bitly.enable")) {
                throw new Exception("Bitly is not enabled.");
            }
            if(!Config.has("bitly.token")) {
                throw new Exception("Bitly token was not found.");
            }
            Bitly bitly = new Bitly(Config.getString("bitly.token"));
            if(!bitly.bitlinks().shorten(link).isPresent()) {
                throw new Exception("Bitly failed to shorten the link.");
            }

            String shortLink = bitly.bitlinks().shorten(link).get().getLink();
            if (shortLink == null || shortLink.isEmpty()) {
                Willy.getLogger().fine(STR."function call shortenLink \{link} failed.");
                return "Something didn't work right.";
            }
            if (shortLink.equals(link)) {
                Willy.getLogger().fine(STR."function call shortenLink \{link} returned the same link.");
                return "Maybe it's already short enough.";
            }

            Willy.getLogger().fine(STR."function call shortenLink \{link} returned \{shortLink}.");
            return shortLink;
        } catch (Exception e) {
            Willy.getLogger().warning(e.getMessage());
            return "Something didn't work right.";
        }
    }
}
