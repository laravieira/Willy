package me.laravieira.willy.openai;

import com.theokanning.openai.OpenAiService;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.*;

public class OpenAi implements WillyChat {
    public static final String ENGINE = "text-davinci-002";
    public static final int BEST_OF = 1;
    public static final int MAX_TOKENS = 50;
    public static final double FREQUENCY_PENALTY = .31;
    public static final double PRESENCE_PENALTY = 1;
    public static final double TEMPERATURE = 1;
    public static final double TOP_P = .19;
    public static final boolean ECHO = true;

    private static OpenAiService service = null;

    public OpenAi() {
        List<String> portugueseHeader = new ArrayList<>();
        portugueseHeader.add("Você é Willy, um cãozinho virtual falante");
        portugueseHeader.add("Sua dona é a Lara Vieira");
        portugueseHeader.add("Você gosta de brincar e você gosta de música");
        portugueseHeader.add("Você é empático, carismático, muito fofo");

        List<String> englishHeader = new ArrayList<>();
        englishHeader.add("You are Willy, a little virtual talking dog");
        englishHeader.add("Your owner is Lara Vieira");
        englishHeader.add("You like to play and you like music");
        englishHeader.add("You are empathetic, charismatic, very cute");

        Map<String, List<String>> headerBaseList = new HashMap<>();
        headerBaseList.put("default", portugueseHeader);
        headerBaseList.put("pt-br", portugueseHeader);
        headerBaseList.put("en-us", englishHeader);
        OpenAiHeader.setHeaderBaseList(headerBaseList);

        Map<String, String> connectorList = new HashMap<>();
        connectorList.put("default", "Complete a conversa:\r\n\r\nWilly: Oi, que bom que você veio falar comigo.\r\n");
        connectorList.put("pt-br", "Complete a conversa:\r\n\r\nWilly: Oi, que bom que você veio falar comigo.\r\n");
        connectorList.put("en-us", "Complete the conversation:\r\n\r\nWilly: Hi, I'm glad you came talk to me.\r\n");
        OpenAiHeader.setConnectorList(connectorList);
    }

    @Override
    public void connect() {
        if(!Config.getBoolean("openai.enable"))
            return;
        service = new OpenAiService(Config.getString("openai.token"));
    }

    @Override
    public void disconnect() {
        service = null;
    }

    @Override
    public boolean isConnected() {
        return service != null;
    }

    @Override
    public void refresh() {

    }

    public static OpenAiService getService() {
        return service;
    }
}
