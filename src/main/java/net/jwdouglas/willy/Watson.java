package net.jwdouglas.willy;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.DialogNodeAction;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.DialogRuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.DialogSuggestion;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeEntity;
import com.ibm.watson.assistant.v2.model.RuntimeIntent;
import com.ibm.watson.assistant.v2.model.SearchResult;
import com.ibm.watson.assistant.v2.model.SessionResponse;

public class Watson {

	private static Logger          log       = MyLogger.getLogger();
	private static Long            lastRef   = new Date().getTime();
	private static boolean         refreshed = false;
	private static SessionResponse session   = null;
	private static Assistant       service   = null;
	private static String          toprint   = "";
	
	public static void start() {
		if(service == null) {
			IamOptions iamOptions = new IamOptions.Builder().apiKey(Config.getWatsonPassword()).build();
			service = new Assistant("2019-07-25", iamOptions);
		}
		
		if(session == null || session.getSessionId() == null || session.getSessionId().isEmpty())
			createSession();
	}
	
	private static void createSession() {
		if(service == null) {return;}
		CreateSessionOptions cso = new CreateSessionOptions.Builder().assistantId(Config.getWatsonID()).build();
		session = service.createSession(cso).execute().getResult();
		
		MessageInput    msgi = new MessageInput.Builder().text("").build();
		MessageOptions  msgo = new MessageOptions.Builder(Config.getWatsonID(), session.getSessionId()).input(msgi).build();
		service.message(msgo).execute().getResult();
		
		lastRef   = new Date().getTime()+270000;
		refreshed = false;
	}
	
	public static void refreshSession() {
		if(Config.getWatsonKeepAlive() && lastRef < new Date().getTime()) {
			sendMessage("");
			refreshed = true;
			log.info("Watson session refreshed.");
		}
	}

	private static void print(String print) {
		toprint += print;
	}
	
	private static void print(Boolean clear) {
		toprint = "";
	}
	
	public static MessageResponse sendMessage(String message) {
		if(service == null) {return null;}
		try {
			if(refreshed) {
				MessageInput    msgi = new MessageInput.Builder().text("").build();
				MessageOptions  msgo = new MessageOptions.Builder(Config.getWatsonID(), session.getSessionId()).input(msgi).build();
				service.message(msgo).execute().getResult();
				refreshed = false;
			}
			MessageInput    msgi = new MessageInput.Builder().text(message).build();
			MessageOptions  msgo = new MessageOptions.Builder(Config.getWatsonID(), session.getSessionId()).input(msgi).build();
			MessageResponse msgr = service.message(msgo).execute().getResult();
			lastRef   = new Date().getTime()+270000;
			refreshed = false;
			return msgr;
		}catch(NotFoundException e) {
			if(e.getMessage().equalsIgnoreCase("invalid session")) {
				log.info("Watson session expired, creating a new session...");
				createSession();
				MessageInput    msgi = new MessageInput.Builder().text(message).build();
				MessageOptions  msgo = new MessageOptions.Builder(Config.getWatsonID(), session.getSessionId()).input(msgi).build();
				MessageResponse msgr = service.message(msgo).execute().getResult();
				lastRef   = new Date().getTime()+270000;
				refreshed = false;
				return msgr;
			}
			return null;
		}
	}
	
	public static void logMessage(MessageResponse msgr, boolean showNulls) {

		List<RuntimeIntent> intents = msgr.getOutput().getIntents();
		if(intents == null || intents.size() == 0) {
			if(showNulls)
				log.info("| No intents detected. ");
		}else {
			print("| Intents:");
			intents.forEach((intent) -> {print(" "+intent.getIntent());});
			log.info(toprint);
			print(true);
		}

		List<RuntimeEntity> entities = msgr.getOutput().getEntities();
		if(entities == null || entities.size() == 0) {
			if(showNulls)
				log.info("| No entities detected. ");
		}else {
			print("| Entities:");
			entities.forEach((entity) -> {print(" "+entity.getEntity());});
			log.info(toprint);
			print(true);
		}

		List<DialogNodeAction> actions = msgr.getOutput().getActions();
		if(actions == null || actions.size() == 0) {
			if(showNulls)
				log.info("| No actions detected. ");
		}else {
			print("| Actions:");
			actions.forEach((action) -> {print(" "+action.getName());});
			log.info(toprint);
			print(true);
		}
		
		List<DialogRuntimeResponseGeneric> dialogs = msgr.getOutput().getGeneric();
		if(dialogs == null || dialogs.size() == 0) {
			if(showNulls) log.info("| No dialogs detected. ");
		}else {
			log.info("| Dialogs: ");
			dialogs.forEach((dialog) -> {
				String b = "";
				b = dialog.getHeader();              if((b == null && showNulls) || b != null) log.info("| ▪ Header:        "+dialog.getHeader());
				b = dialog.getTitle();               if((b == null && showNulls) || b != null) log.info("| | Title:         "+dialog.getTitle());
				b = dialog.getTopic();               if((b == null && showNulls) || b != null) log.info("| | Topic:         "+dialog.getTopic());
				Long c = dialog.getTime();           if((c == null && showNulls) || c != null) log.info("| | Time:          "+dialog.getTime());
				b = dialog.getPreference();          if((b == null && showNulls) || b != null) log.info("| | Preference:    "+dialog.getPreference());
				b = dialog.getResponseType();        if((b == null && showNulls) || b != null) log.info("| | Response Type: "+dialog.getResponseType());
				b = dialog.getSource();              if((b == null && showNulls) || b != null) log.info("| | Source:        "+dialog.getSource());
				b = dialog.getMessageToHumanAgent(); if((b == null && showNulls) || b != null) log.info("| | Msg To Human:  "+dialog.getMessageToHumanAgent());
				b = dialog.getDescription();         if((b == null && showNulls) || b != null) log.info("| | Description:   "+dialog.getDescription());
				b = dialog.getText();                if((b == null && showNulls) || b != null) log.info("| | Text:          "+dialog.getText());
				List<DialogNodeOutputOptionsElement> options = dialog.getOptions();
				if(options == null || options.size() == 0) {
					if(showNulls)
						log.info("| | No options detected. ");
				}else {
					log.info("| | Options: ");
					options.forEach((option) -> {log.info("| | ▪ "+option.getLabel());});
					log.info(toprint);
					print(true);
				}
				List<SearchResult> results = dialog.getResults();
				if(results == null || results.size() == 0) {
					if(showNulls)
						log.info("| | No results detected. ");
				}else {
					log.info("| | Results: ");
					results.forEach((result) -> {log.info("| | ▪ "+result.getTitle());});
					log.info(toprint);
					print(true);
				}
				List<DialogSuggestion> suggestions = dialog.getSuggestions();
				if(suggestions == null || suggestions.size() == 0) {
					if(showNulls)
						log.info("| | No options detected. ");
				}else {
					log.info("| | Options: ");
					suggestions.forEach((suggestion) -> {log.info("| | ▪ "+suggestion.getLabel());});
					log.info(toprint);
					print(true);
				}
			});
			log.info("");
		}
		
	}
	
	public static void finish() {
		if(service != null && session != null) {
			try {
				DeleteSessionOptions dso = new DeleteSessionOptions.Builder(Config.getWatsonID(), session.getSessionId()).build();
				service.deleteSession(dso).execute();
			}catch(NotFoundException e) {}
		}
	}
	
}
