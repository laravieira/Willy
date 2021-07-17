package me.laravieira.willy.kernel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.ibm.watson.assistant.v2.model.MessageContext;
import com.ibm.watson.assistant.v2.model.MessageContextGlobal;
import com.ibm.watson.assistant.v2.model.MessageContextGlobalSystem;
import com.ibm.watson.assistant.v2.model.MessageContextSkill;
import com.ibm.watson.assistant.v2.model.MessageContextSkillSystem;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.discord.DiscordMessage;
import me.laravieira.willy.watson.WatsonMessage;

public class Context {
	
	private static Map<String, Context> contexts = new HashMap<String, Context>();
	private Map<Long, Message> messages = new HashMap<Long, Message>();
	private MessageChannel channel = null;
	private User user = null;
	private Message discord_message = null;
	private Logger logger = null;
	private MessageContext context = null;
	private WatsonMessage watson_message = null;
	private int consecutive_calls = 0;
	private long expire = 0;
	private String id = null;
	private boolean del_msgs = true;
	private boolean debug_msg = false;
	
	public static Context getContext(String id) {
		if(contexts.containsKey(id))
			return contexts.get(id);
		else return null;
	}
	
	public static Context getDiscordContext(MessageChannel channel, Message message, User user, String id) {
		if(contexts.containsKey(id)) {
			Context context = contexts.get(id);
			context.saveDiscordMessage(message);
			context.setDiscordChannel(channel);
			context.setDiscordMessage(message);
			context.setDiscordUser(user);
			return context;
		}else {
			Context context = new Context(channel, message, user, id);
			contexts.put(id, context);
			return contexts.get(id);
		}
	}
	
	public static Context getConsoleContext() {
		if(contexts.containsKey("willy-console"))
			return contexts.get("willy-console");
		else {
			Context context = new Context(MyLogger.getConsoleLogger());
			contexts.put("willy-console", context);
			return contexts.get("willy-console");
		}
	}

	public static Context getInternalContext() {
		if(contexts.containsKey("willy-internal"))
			return contexts.get("willy-internal");
		else {
			Context context = new Context(MyLogger.getLogger());
			contexts.put("willy-internal", context);
			return contexts.get("willy-internal");
		}
	}

	public static void refresh() {
		try {
		long now = new Date().getTime()+10000;
		List<Context> delContexts = new ArrayList<Context>();
		contexts.forEach((identifier, context) -> {
			if(context.getContextExpire() < now)
				delContexts.add(context);
			if(Config.getClearChats() && context.getDeleteMessages()) {
				Map<Long, Message> delMessages = new HashMap<Long, Message>();
				context.getDiscordMessages().forEach((key, value) -> {
					if(key < now) {
						value.delete("Time expired.").block();
						MyLogger.getLogger().info("Message deleted from a public chat.");
						delMessages.put(key, value);
					}
				});
				delMessages.forEach((key, value) -> {
					context.getDiscordMessages().remove(key, value);
				});
			}
		});
		delContexts.forEach((context) -> {
			if(Config.getClearChats() && context.getDeleteMessages())
				context.getDiscordMessages().forEach((timestamp, message) -> {
					DiscordMessage.addMessage(timestamp, message);
				});
			contexts.remove(context.getId());
		});
		}catch(RuntimeException e) {}
	}
	
	public static Map<String, Context> getContexts() {
		return contexts;
	}

	public Context(MessageChannel channel, Message message, User user, String id) {
		saveDiscordMessage(message);
		this.channel = channel;
		this.discord_message = message;
		this.user = user;
		buildContext(id);
	}
	
	public Context(Logger logger) {
		this.logger = logger;
		buildContext("willy-console");
	}

	private void buildContext(String id) {
		expire = new Date().getTime()+Config.getContextLifeTime();
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
				.userDefined(new HashMap<String, Object>())
				.system(skillContextSystem)
				.build();
		
		Map<String, MessageContextSkill> messagesContextSkill = new HashMap<String, MessageContextSkill>();
		messagesContextSkill.put("main skill", skillContext);

		context = new MessageContext.Builder()
				.global(globalContext)
				.skills(messagesContextSkill)
				.build();
		
		watson_message = new WatsonMessage(context, id);
	}
	
	public void saveDiscordMessage(Message message) {
		long expire = Config.getClearTime() + new Date().getTime();
		messages.put(expire, message);
	}

	public void setWatsonContext(MessageContext context) {
		expire = new Date().getTime()+Config.getContextLifeTime();
		this.context = context;
	}
	
	public void setContextSystem(MessageContextGlobalSystem system) {
		expire = new Date().getTime()+Config.getContextLifeTime();
		context.global().newBuilder().system(system).build();
	}
	
	public void setContextSkill(MessageContextSkill skill) {
		expire = new Date().getTime()+Config.getContextLifeTime();
		context.skills().remove("main skill");
		context.skills().put("main skill", skill);
	}
	
	public void setUserDefined(Map<String, Object> userDefined) {
		MessageContextSkill mainSkill = context.skills().get("main skill");
		userDefined.putAll(mainSkill.userDefined());
		mainSkill.userDefined().putAll(userDefined);
		context.skills().put("main skill", mainSkill);
	}

	public void setWatsonMessage(WatsonMessage messager) {
		this.watson_message = messager;
	}
	
	public void setDiscordChannel(MessageChannel channel) {
		this.channel = channel;
	}
	
	public void setDiscordUser(User user) {
		this.user = user;
	}
	
	public void setDiscordMessage(Message message) {
		this.discord_message = message;
	}
	
	public void setDeleteMessages(boolean value) {
		del_msgs = value;
	}
	
	public void setDebugWatsonMessage(boolean value) {
		debug_msg = value;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
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
	
	public long getContextExpire() {
		return expire;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public MessageChannel getDiscordChannel() {
		return channel;
	}
	
	public User getDiscordUser() {
		return user;
	}
	
	public Message getDiscordMessage() {
		return discord_message;
	}
	
	public Map<Long, Message> getDiscordMessages() {
		return messages;
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
