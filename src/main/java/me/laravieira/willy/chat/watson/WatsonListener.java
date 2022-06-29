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
    public void onTextMessageResponse(MessageResponse response, RuntimeResponseGeneric generic, String ctxId) {
        UUID context = UUID.fromString(ctxId);
        ContextStorage.of(context).getWatson().setWatsonContext(response.getContext());

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setFrom("Willy");
        message.setTo(ContextStorage.of(context).getLastMessage().getFrom());
        message.setText(generic.text());
        message.setContent(generic);
        MessageStorage.add(message);

        ContextStorage.of(context).getSender().sendText(message.getText());
    }

    public void onActionResponse(@NotNull MessageResponse response, DialogNodeAction action, String ctxId) {
        UUID context = UUID.fromString(ctxId);
        ContextStorage.of(context).getWatson().setWatsonContext(response.getContext());
        new WatsonAction(action, context);
    }

    public void onErrorResponse() {
        Willy.getLogger().warning("Watson response fail.");
    }
}
