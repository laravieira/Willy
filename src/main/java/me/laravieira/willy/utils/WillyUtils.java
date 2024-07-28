package me.laravieira.willy.utils;

import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.MessageStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WillyUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasWillyName(@NotNull String message, @NotNull List<String> calls) {
        for(String alias : calls)
            if(message.contains(alias))
                return true;
        return false;
    }

    public static boolean startsWith(@NotNull String message, @NotNull List<String> starts) {
        for(String prefix : starts)
            if(message.startsWith(prefix))
                return true;
        return false;
    }


    public static List<ChatMessage> parseContextToOpenAIChat(@NotNull LinkedList<UUID> messages, int historySize) throws MalformedURLException, URISyntaxException {
        List<ChatMessage> chat = new ArrayList<>();
        LinkedList<UUID> lastMessages = new LinkedList<>(messages);
        LinkedList<Message> descendingHistory = new LinkedList<>();
        for(int i = 0; i < lastMessages.size() && i < historySize; i++)
            descendingHistory.add(MessageStorage.of(lastMessages.pollLast()));
        Iterator<Message> history = descendingHistory.descendingIterator();

        while(history.hasNext()) {
            Message message = history.next();
            if(message.getFrom().equals(Willy.getWilly().getName()))
                chat.add((ChatMessage.ResponseMessage) message.getContent());
            else if(message.getFrom().equals("SYSTEM"))
                chat.add((ChatMessage.ToolMessage) message.getContent());
            else {
                for(File image : message.getAttachments()) {
                    ContentPart.ContentPartImageUrl.ImageUrl imageUrl = loadImageAsBase64(image.toPath());
                    if(imageUrl != null) {
                        ChatMessage.UserMessage usermessage = ChatMessage.UserMessage.of(List.of(
                            ContentPart.ContentPartText.of(message.getText()),
                            ContentPart.ContentPartImageUrl.of(imageUrl)
                        ));
                        chat.add(usermessage);
                    }
                }
                for(String image : message.getUrls()) {
                    ContentPart.ContentPartImageUrl.ImageUrl imageUrl = loadImageAsBase64(Path.of(URI.create(image)));
                    if(image != null) {
                        ChatMessage.UserMessage usermessage = ChatMessage.UserMessage.of(List.of(
                            ContentPart.ContentPartText.of(message.getText()),
                            ContentPart.ContentPartImageUrl.of(ContentPart.ContentPartImageUrl.ImageUrl.of(image))
                        ));
                        chat.add(usermessage);
                    }
                }
            }
        }
        return chat;
    }

    private static ContentPart.ContentPartImageUrl.ImageUrl loadImageAsBase64(Path image) {
        try {
            byte[] imageBytes = Files.readAllBytes(image);
            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            var extension = image.getFileName().toString().substring(image.getFileName().toString().lastIndexOf('.') + 1);
            var prefix = "data:image/" + extension + ";base64,";
            return ContentPart.ContentPartImageUrl.ImageUrl.of(prefix + base64String);
        } catch (Exception e) {
            Willy.getLogger().warning("Error loading image as base64: " + e.getMessage());
            return null;
        }
    }
}
