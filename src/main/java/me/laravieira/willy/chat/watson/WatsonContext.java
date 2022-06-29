package me.laravieira.willy.chat.watson;

import com.ibm.watson.assistant.v2.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public class WatsonContext {
    private MessageContext context;
    private WatsonMessage watson_message;

    public WatsonContext(@NotNull UUID context) {

        MessageContextGlobalSystem system = new MessageContextGlobalSystem.Builder()
                .timezone(TimeZone.getDefault().getID())
                .userId(context.toString())
                .build();

        MessageContextGlobal globalContext = new MessageContextGlobal.Builder()
                .system(system)
                .build();

        MessageContextSkillSystem skillContextSystem = new MessageContextSkillSystem();

        MessageContextSkill skillContext = new MessageContextSkill.Builder()
                .userDefined(new HashMap<>())
                .system(skillContextSystem)
                .build();

        Map<String, MessageContextSkill> messagesContextSkill = new HashMap<>();
        messagesContextSkill.put("main skill", skillContext);

        this.context = new MessageContext.Builder()
                .global(globalContext)
                .skills(messagesContextSkill)
                .build();

        watson_message = new WatsonMessage(this.context, context.toString());
    }

    public void setWatsonContext(MessageContext context) {
        this.context = context;
    }

    public void setContextSystem(MessageContextGlobalSystem system) {
        context.global().newBuilder().system(system).build();
    }

    public void setContextSkill(MessageContextSkill skill) {
        context.skills().remove("main skill");
        context.skills().put("main skill", skill);
    }

    public void setUserDefined(@NotNull Map<String, Object> userDefined) {
        MessageContextSkill mainSkill = context.skills().get("main skill");
        userDefined.putAll(mainSkill.userDefined());
        mainSkill.userDefined().putAll(userDefined);
        context.skills().put("main skill", mainSkill);
    }

    public void setWatsonMessage(WatsonMessage messager) {
        this.watson_message = messager;
    }

    public MessageContext getWatsonContext() {
        return context;
    }

    public MessageContextGlobalSystem getContextSystem() {
        return context.global().system();
    }

    public MessageContextSkill getContextSkill() {
        return context.skills().get("main skill");
    }

    public WatsonMessage getWatsonMessager() {
        return watson_message;
    }

    public Map<String, Object> getUserDefined() {
        return context.skills().get("main skill").userDefined();
    }
}
