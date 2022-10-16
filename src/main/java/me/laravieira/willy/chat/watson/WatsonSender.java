package me.laravieira.willy.chat.watson;

import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.assistant.v2.model.*;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class WatsonSender implements SenderInterface {
    private final UUID context;
    private final MessageContext messageContext;

    public WatsonSender(UUID context, MessageContext messageContext) {
        this.context = context;
        this.messageContext = messageContext;
    }

    private void send(MessageInput input) {
        try {
            if(!new Watson().isConnected())
                Watson.registrySession();
            MessageOptions options = Watson.getMessageBuilder().input(input).context(messageContext).build();
            MessageResponse response = Watson.getService().message(options).execute().getResult();
            Watson.resetTimeout();
            new WatsonListener().onMessageResponse(response, context);
        }catch (NotFoundException e) {
            if(!e.getMessage().contains("Invalid Session"))
                e.printStackTrace();
            Watson.registrySession();
            MessageOptions options = Watson.getMessageBuilder().input(input).context(messageContext).build();
            MessageResponse response = Watson.getService().message(options).execute().getResult();
            Watson.resetTimeout();
            new WatsonListener().onMessageResponse(response, context);
        }
    }

    public void sendAction(String name, Object value) {
        Map<String, Object> userDefined = messageContext.skills().get("main skill").userDefined();
        userDefined.put(name, value);
        messageContext.skills().get("main skill").userDefined().putAll(userDefined);

        MessageInputOptions inputOptions = new MessageInputOptions.Builder().returnContext(true).build();

        MessageInput input = new MessageInput.Builder()
                .messageType("text").text(null)
                .options(inputOptions).build();
        send(input);
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        MessageInputOptions inputOptions = new MessageInputOptions.Builder().returnContext(true).build();
        MessageInput input = new MessageInput.Builder()
                .messageType("text").text(message).options(inputOptions).build();
        send(input);
    }

    @Override
    public void sendLink(Message message) {

    }

    @Override
    public void sendStick(Message message) {

    }

    @Override
    public void sendGif(Message message) {

    }

    @Override
    public void sendImage(Message message) {

    }

    @Override
    public void sendVideo(Message message) {

    }

    @Override
    public void sendAudio(Message message) {

    }

    @Override
    public void sendLocation(Message message) {

    }

    @Override
    public void sendContact(Message message) {

    }

    @Override
    public void sendFile(File message) {

    }
    @SuppressWarnings("unused")
    public void debug(@NotNull Logger log, MessageResponse response) {
        assert false;
        MessageOutput output = response.getOutput();
        log.info(" ▪ Timezone: "+messageContext.global().system().timezone());
        log.info(" ▪ TurnCount: "+messageContext.global().system().turnCount());
        log.info(" ▪ UserId: "+messageContext.global().system().userId());
        Map<String, MessageContextSkill> skills = messageContext.skills();
        if(skills != null) {
            log.info(" ▪ Skills: "+skills.size());
            skills.forEach((name, skill) -> log.info("       - "+name+": "+skill.toString()));
        }else log.info(" ▪ Skills: null");
        Map<String, Object> userDefined = output.getUserDefined();
        if(userDefined != null) {
            log.info(" ▪ UserVariables: "+userDefined.size());
            userDefined.forEach((key, value) -> log.info("       - "+key+": "+value.toString()));
        }else log.info(" ▪ UserVariables: null");
        List<RuntimeIntent> intents = output.getIntents();
        if(intents != null) {
            log.info(" • Intents: "+intents.size());
            intents.forEach((intent) -> log.info("       - "+intent.intent()));
        }else log.info(" • Intents: null");
        List<RuntimeEntity> entities = output.getEntities();
        if(entities != null) {
            log.info(" • Entities: "+entities.size());
            entities.forEach((entity) -> log.info("       - "+entity.entity()+": "+entity.value()));
        }else log.info(" • Entities: null");
        List<DialogNodeAction> actions = output.getActions();
        if(actions != null) {
            log.info(" • Actions: "+actions.size());
            actions.forEach((action) -> {
                log.info("       - "+action.getName());
                log.info("             + Type: "       +action.getType());
                log.info("             + Result: "     +action.getResultVariable());
                log.info("             + Credentials: "+action.getCredentials());
                Map<String, Object> parameters = action.getParameters();
                if(parameters != null) {
                    log.info("             + Parameters: "+parameters.size());
                    parameters.forEach((key, value) -> log.info("                - "+(key==null?"null":key)+": "+(value==null?"null":value.toString())));
                }else log.info("             + Parameters: null");
            });
        }else log.info(" • Actions: null");
        List<RuntimeResponseGeneric> dialogs = output.getGeneric();
        if(dialogs != null) {
            log.info(" • Dialogs: "+dialogs.size());
            dialogs.forEach((dialog) -> {
                log.info("       - "+dialog.getClass().toGenericString());
                String space = "             ";

                Map<String, String> subDialog = new HashMap<>();
                subDialog.put("Header",        dialog.header());
                subDialog.put("Title",         dialog.title());
                subDialog.put("Topic",         dialog.topic());
                subDialog.put("Time",         (dialog.time() == null)?null:(""+dialog.time()));
                subDialog.put("Preference",    dialog.preference());
                subDialog.put("Response Type", dialog.responseType());
                subDialog.put("Source",        dialog.source());
                subDialog.put("Msg To Human",  dialog.messageToHumanAgent());
                subDialog.put("Description",   dialog.description());
                subDialog.put("Text",          dialog.text());

                subDialog.forEach((name, vars) -> {
                    if(vars == null || vars.isEmpty())
                        log.info(space+"+ "+name+": null");
                    else
                        log.info(space+"+ "+name+" ["+vars.length()+"]: "+vars);
                });

                List<DialogNodeOutputOptionsElement> options = dialog.options();
                if(options != null) {
                    log.info(space+"• Options: "+options.size());
                    options.forEach((option) -> log.info(space+"      - "+option.getLabel()+": "+option.getValue()));
                }else log.info(" • Options: null");
                List<SearchResult> results = dialog.primaryResults();
                if(results != null) {
                    log.info(space+"• Results: "+results.size());
                    results.forEach((result) -> log.info(space+"      - "+result.getTitle()+": "+result.getBody()));
                }else log.info(" • Results: null");
                List<DialogSuggestion> suggestions = dialog.suggestions();
                if(suggestions != null) {
                    log.info(space+"• Suggestions: "+suggestions.size());
                    suggestions.forEach((suggestion) -> log.info(space+"      - "+suggestion.getLabel()+": "+suggestion.getValue()));
                }else log.info(" • Suggestions: null");
            });
        }else log.info(" • Dialogs: null");
        log.info("");
    }
}
