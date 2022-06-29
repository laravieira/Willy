package me.laravieira.willy.chat.watson;

import java.util.UUID;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.utils.PassedInterval;

public class Watson implements WillyChat {

	private static boolean   registered = false;
	private static Assistant service    = null;
	private static String    session    = null;
	private static final PassedInterval expire = new PassedInterval(Config.getLong("wa.session_live"));

	public static void registrySession() {
		CreateSessionOptions cso = new CreateSessionOptions.Builder().assistantId(Config.getString("wa.assistant_id")).build();
		SessionResponse sessionResponse = Watson.getService().createSession(cso).execute().getResult();
		expire.reset();
		session = sessionResponse.getSessionId();
		registered = true;
		Willy.getLogger().info("Watson instance opened.");
	}

	public static void resetTimeout() {
		expire.reset();
	}

	public static Assistant getService() {
		return service;
	}

	public static String getSessionId() {
		return session;
	}

	public static MessageOptions.Builder getMessageBuilder() {
		return new MessageOptions.Builder(Config.getString("wa.assistant_id"), Watson.getSessionId());
	}

	@Override
	public void refresh() {
		if(Config.getBoolean("wa.keep_alive") && expire.hasPassedInterval()) {
			UUID context = UUID.nameUUIDFromBytes("willy-refresh".getBytes());
			ContextStorage.of(context).getWatson().getSender().sendText(null);
		}else if(expire.hasPassedInterval()) {
			registered = false;
			session = null;
		}
	}

	@Override
	public void connect() {
		IamAuthenticator iamAuth = new IamAuthenticator.Builder().apikey(Config.getString("wa.password")).build();
		service = new Assistant(Config.getString("wa.api_date"), iamAuth);
		service.setServiceUrl(Config.getString("wa.server_url"));
		if(Config.getBoolean("wa.keep_alive"))
			registrySession();
		expire.start();
		Willy.getLogger().info("Watson instance opened.");
	}

	@Override
	public void disconnect() {
		if(session != null) {
			try {
				DeleteSessionOptions dso = new DeleteSessionOptions.Builder(Config.getString("wa.assistant_id"), session).build();
				service.deleteSession(dso).execute();
				registered = false;
			}catch(NotFoundException ignore) {}
		}
	}

	@Override
	public boolean isConnected() {
		return registered;
	}
}
