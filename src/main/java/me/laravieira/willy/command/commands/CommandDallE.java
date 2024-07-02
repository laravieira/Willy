package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.sashirestela.openai.domain.image.Image;
import io.github.sashirestela.openai.domain.image.ImageRequest;
import io.github.sashirestela.openai.domain.image.ImageResponseFormat;
import io.github.sashirestela.openai.domain.image.Size;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.internal.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandDallE implements CommandListener {
    public static final String COMMAND = "dall-e";
    public static final String DESCRIPTION = "Generate an image using OpenAI's DALL-E 2.";
    public static final String PROMPT = "prompt";
    public static final String AMOUNT = "amount";
    public static final String STYLE = "style";
    public static final String SIZE = "size";

    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .addOption(ApplicationCommandOptionData.builder()
                .name(PROMPT)
                .description("The prompt to generate the image.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build()
            )
            .addOption(ApplicationCommandOptionData.builder()
                .name(STYLE)
                .description("Generation style.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("natural").value(ImageRequest.Style.NATURAL.name()).build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("vivid").value(ImageRequest.Style.VIVID.name()).build())
                .required(false)
                .build()
            )
            .addOption(ApplicationCommandOptionData.builder()
                .name(SIZE)
                .description("Image size.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("256").value(Size.X256.name()).build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("512").value(Size.X512.name()).build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("1024").value(Size.X1024.name()).build())
                .required(false)
                .build()
            )
            .addOption(ApplicationCommandOptionData.builder()
                .name(AMOUNT)
                .description("The amount of images to generate.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("1").value("1").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("2").value("2").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("3").value("3").build())
                .required(false)
                .build()
            ).build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        if(!Config.getBoolean("openai.dall_e")) {
            event.reply("Dall-E is not enabled.").subscribe();
            return;
        }
        if(event.getOptions().isEmpty()) {
            event.reply("You need to provide a prompt.").subscribe();
            return;
        }
        String prompt = "";
        ImageRequest.Style style = ImageRequest.Style.NATURAL;
        Size size = Size.X256;
        int amount = 1;

        if(event.getOption(PROMPT).isPresent() && event.getOption(PROMPT).get().getValue().isPresent())
            prompt = event.getOption(PROMPT).get().getValue().get().asString();
        if(event.getOption(STYLE).isPresent() && event.getOption(STYLE).get().getValue().isPresent())
            style = ImageRequest.Style.valueOf(event.getOption(STYLE).get().getValue().get().asString());
        if(event.getOption(SIZE).isPresent() && event.getOption(SIZE).get().getValue().isPresent())
            size = Size.valueOf(event.getOption(SIZE).get().getValue().get().asString());
        if(event.getOption(AMOUNT).isPresent() && event.getOption(AMOUNT).get().getValue().isPresent())
            amount = Integer.parseInt(event.getOption(AMOUNT).get().getValue().get().asString());

        event.reply("Generating...").subscribe();
        try {
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
                event.reply("An error occurred while generating the image.").subscribe();
                return;
            }
            event.editReply("Here are the images generated:").subscribe();
            for(Image image : images) {
                Willy.getLogger().fine(STR."Dall-E generated image: \{image.getUrl()}");
                event.createFollowup(image.getUrl()).block();
            }
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Dal-E error: \{e.getMessage()}");
            event.editReply("An error occurred while generating the image.").subscribe();
        }
    }

    @Override
    public String getName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
