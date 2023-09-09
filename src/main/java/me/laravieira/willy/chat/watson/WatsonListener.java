package me.laravieira.willy.chat.watson;

import com.ibm.watson.assistant.v2.model.DialogNodeAction;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WatsonListener {
    public void onMessageResponse(@NotNull MessageResponse response, UUID context) {
        if(response.getContext() == null || response.getOutput() == null) {
            Willy.getLogger().warning("Watson response fail.");
            return;
        }

        if(response.getOutput().getGeneric() != null && !response.getOutput().getGeneric().isEmpty())
            response.getOutput().getGeneric().forEach((generic) -> {
                if(generic.responseType().equalsIgnoreCase("text"))
                    new WatsonListener().onTextResponse(response, generic, context);
            });

        if(response.getOutput().getActions() != null && !response.getOutput().getActions().isEmpty())
            response.getOutput().getActions().forEach(
                    (action) -> new WatsonListener().onActionResponse(response, action, context));
    }

    private void onTextResponse(@NotNull MessageResponse response, @NotNull RuntimeResponseGeneric generic, UUID context) {
//        ContextStorage.of(context).getWatson().setWatsonContext(response.getContext());

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setFrom(Willy.getWilly().getName());
        message.setTo(ContextStorage.of(context).getLastMessage().getFrom());
        message.setText(generic.text());
        message.setContent(generic);
        MessageStorage.add(message);

        ContextStorage.of(context).getSender().sendText(message.getText());
    }

    private void onActionResponse(@NotNull MessageResponse response, DialogNodeAction action, UUID context) {
//        ContextStorage.of(context).getWatson().setWatsonContext(response.getContext());
        new WatsonAction(action, context);
    }
}
