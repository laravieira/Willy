package me.laravieira.willy.kernel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.ibm.watson.assistant.v2.model.MessageContext;
import com.ibm.watson.assistant.v2.model.MessageContextGlobal;
import com.ibm.watson.assistant.v2.model.MessageContextGlobalSystem;
import com.ibm.watson.assistant.v2.model.MessageContextSkill;
import com.ibm.watson.assistant.v2.model.MessageContextSkillSystem;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.watson.WatsonMessage;
import org.apache.commons.lang3.NotImplementedException;

public class Context {
	private static final Map<String, Context> contexts = new HashMap<>();
	private static long contextLifeTimestamp = Config.getLong("context_lifetime");

	private MessageContext context;
	private WatsonMessage watson_message;
	private String id;

	private String userLanguage = "default";
	private int consecutive_calls = 0;
	private boolean del_msgs = true;
	private boolean debug_msg = false;
	private long expire;

	public static Context getContext(String id) {
		return contexts.getOrDefault(id, null);
	}

	public static void refresh() {
		try {
			long now = new Date().getTime()+10000;
			List<Context> delContexts = new ArrayList<>();
			contexts.forEach((identifier, context) -> {
				if(context.getContextExpire() < now)
					delContexts.add(context);
			});
			delContexts.forEach((context) -> contexts.remove(context.getId()));
		}catch(RuntimeException ignored) {}
	}

	public Map<?, ?> getMessages() {
		throw new NotImplementedException("Global Context do not registry messages, call sub-contexts.");
	}

	public Sender getSender() {
		throw new NotImplementedException("Global context do not send messages, call sub-contexts.");
	}

	public void deleteMessage(Object message) {
		throw new NotImplementedException("Global context do not registry messages to be able to delete, call sub-contexts.");
	}

	public static Map<String, Context> getContexts() {
		return contexts;
	}



	public Context(String id) {
		expire = new Date().getTime()+contextLifeTimestamp;
		this.id = id;
		
		MessageContextGlobalSystem system = new MessageContextGlobalSystem.Builder()
				.timezone(TimeZone.getDefault().getID())
				.userId(id)
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

		context = new MessageContext.Builder()
				.global(globalContext)
				.skills(messagesContextSkill)
				.build();
		
		watson_message = new WatsonMessage(context, id);
	}

	public void setWatsonContext(MessageContext context) {
		expire = new Date().getTime()+contextLifeTimestamp;
		this.context = context;
	}
	
	public void setContextSystem(MessageContextGlobalSystem system) {
		expire = new Date().getTime()+contextLifeTimestamp;
		context.global().newBuilder().system(system).build();
	}
	
	public void setContextSkill(MessageContextSkill skill) {
		expire = new Date().getTime()+contextLifeTimestamp;
		context.skills().remove("main skill");
		context.skills().put("main skill", skill);
	}
	
	public void setUserDefined(Map<String, Object> userDefined) {
		MessageContextSkill mainSkill = context.skills().get("main skill");
		userDefined.putAll(mainSkill.userDefined());
		mainSkill.userDefined().putAll(userDefined);
		context.skills().put("main skill", mainSkill);
	}

	public void setUserLanguage(String userLanguage) {this.userLanguage = userLanguage;}

	public void setWatsonMessage(WatsonMessage messager) {
		this.watson_message = messager;
	}

	public void setDeleteMessages(boolean value) {
		del_msgs = value;
	}
	
	public void setDebugWatsonMessage(boolean value) {
		debug_msg = value;
	}

	public void addConsecutiveCall() {
		consecutive_calls++;
	}
	
	public int getConsecutiveCall() {
		return consecutive_calls;
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

	public String getUserLanguage() {return userLanguage;}

	public long getContextExpire() {
		return expire;
	}

	public boolean getDebugWatsonMessage() {
		return debug_msg;
	}
	
	public boolean getDeleteMessages() {
		return del_msgs;
	}
	
	public String getId() {
		return id;
	}

}
