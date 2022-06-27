package me.laravieira.willy.watson;

import com.ibm.watson.assistant.v2.model.DialogNodeAction;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import me.laravieira.willy.Willy;
import me.laravieira.willy.kernel.Context;

public class WatsonListener {
    public void onTextMessageResponse(MessageResponse response, RuntimeResponseGeneric message, String ctxId) {
        Context context = Context.getContext(ctxId);
        context.setWatsonContext(response.getContext());
        context.getSender().send(message.text());
    }

    public void onActionResponse(MessageResponse response, DialogNodeAction action, String ctxId) {
        Context context = Context.getContext(ctxId);
        context.setWatsonContext(response.getContext());
        new WatsonAction(action, ctxId);
    }

    public void onErrorResponse(MessageResponse response) {
        Willy.getLogger().warning("Watson response fail.");
    }
}
