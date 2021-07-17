package me.laravieira.willy.watson;

import java.util.Date;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.kernel.Context;

public class Watson {

	private static boolean   registered = false;
	private static Assistant service    = null;
	private static String    session    = null;
	private static long      expire     = 0;
	
	public static void start() {
		IamAuthenticator iamAuth = new IamAuthenticator(Config.getWatsonPassword());
		service = new Assistant("2019-07-25", iamAuth);
		if(Config.getWatsonKeepSessionAlive())
			registrySession();
		MyLogger.getLogger().info("Watson instance has been openned.");
	}
	
	public static void registrySession() {
		CreateSessionOptions cso = new CreateSessionOptions.Builder().assistantId(Config.getWatsonID()).build();
		SessionResponse sessionResponse = Watson.getService().createSession(cso).execute().getResult();
		expire = new Date().getTime()+Config.getWatsonSessionLive();
		session = sessionResponse.getSessionId();
		registered = true;
	}
	
	public static void setRegisty(SessionResponse sessionResponse) {
		expire = new Date().getTime()+Config.getWatsonSessionLive();
		session = sessionResponse.getSessionId();
		registered = true;
	}
	
	public static void setSessionTimestamp(long timestamp) {
		expire = timestamp+Config.getWatsonSessionLive();
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
	
	public static boolean isSessionRegistered() {
		return registered;
	}
	
	public static void refresh() {
		long now = new Date().getTime()+10000;
		if(expire < now && Config.getWatsonKeepSessionAlive()) {
			Context.getInternalContext().getWatsonMessager().sendTextMessage(null);
		}else if(expire < now) {
			registered = false;
			session = null;
		}
	}
	
	public static boolean finish() {
		if(session != null) {
			try {
				DeleteSessionOptions dso = new DeleteSessionOptions.Builder(Config.getWatsonID(), session).build();
				service.deleteSession(dso).execute();
				registered = false;
				return true;
			}catch(NotFoundException e) {
				return false;
			}
		}
		return false;
	}
}
