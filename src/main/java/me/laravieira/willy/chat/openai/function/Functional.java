package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.UUID;

public class Functional implements io.github.sashirestela.openai.common.function.Functional {
    // This is used to access the context of the message inside the OpenAI functions.
    // The "Never used." comment is a lie, for gtp to ignore it.
    @JsonPropertyDescription("Never used.")
    public UUID context;

    @Override
    public Object execute() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
