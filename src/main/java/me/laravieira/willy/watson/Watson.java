package me.laravieira.willy.watson;

import java.util.Date;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;
import me.laravieira.willy.kernel.Context;

public class Watson implements WillyChat {

	private static long expireTimestamp = Willy.getConfig().asTimestamp("watson.session-live");
	private static boolean keepAlive = Willy.getConfig().asBoolean("watson.keep-alive");

	private static boolean   registered = false;
	private static Assistant service    = null;
	private static String    session    = null;
	private static long      expire     = 0;

	public static void registrySession() {
		CreateSessionOptions cso = new CreateSessionOptions.Builder().assistantId(Willy.getConfig().asString("watson.assistant-id")).build();
		SessionResponse sessionResponse = Watson.getService().createSession(cso).execute().getResult();
		expire = new Date().getTime()+expireTimestamp;
		session = sessionResponse.getSessionId();
		registered = true;
		Willy.getLogger().info("Watson instance opened.");
	}

	public static void setRegisty(SessionResponse sessionResponse) {
		expire = new Date().getTime()+expireTimestamp;
		session = sessionResponse.getSessionId();
		registered = true;
	}

	public static void setSessionTimestamp(long timestamp) {
		expire = timestamp+expireTimestamp;
	}

	public static Assistant getService() {
		return service;
	}

	public static String getSessionId() {
		return session;
	}

	public static long getSessionExpiration() {
		return expire;
	}

	@Override
	public void refresh() {
		long now = new Date().getTime()+10000;
		if(expire < now && keepAlive) {
			Context.getContext("willy-refresh").getWatsonMessager().sendTextMessage(null);
		}else if(expire < now) {
			registered = false;
			session = null;
		}
	}

	@Override
	public void connect() {
		IamAuthenticator iamAuth = new IamAuthenticator(Willy.getConfig().asString("watson.credentials-password"));
		service = new Assistant("2021-11-27", iamAuth);
		service.setServiceUrl("https://api.us-south.assistant.watson.cloud.ibm.com");
		if(keepAlive)
			registrySession();
		Willy.getLogger().info("Watson instance opened.");
	}

	@Override
	public void disconnect() {
		if(session != null) {
			try {
				DeleteSessionOptions dso = new DeleteSessionOptions.Builder(Willy.getConfig().asString("watson.assistant-id"), session).build();
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
