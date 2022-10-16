package me.laravieira.willy.chat.openai;

import com.theokanning.openai.OpenAiService;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.*;

public class OpenAi implements WillyChat {
    public static final String ENGINE = "text-davinci-002";
    public static final int BEST_OF = 1;
    public static final int MAX_TOKENS = 100;
    public static final double FREQUENCY_PENALTY = .51;
    public static final double PRESENCE_PENALTY = 1;
    public static final double TEMPERATURE = 1;
    public static final double TOP_P = .19;
    public static final boolean ECHO = false;
    public static final int HISTORY_SIZE = 7;

    private static OpenAiService service = null;

    public OpenAi() {
        List<String> portugueseHeader = new ArrayList<>();
        portugueseHeader.add("Você é Willy, um cãozinho virtual falante da Lara Vieira. Você é muito fofo.");
        portugueseHeader.add("Vocé é branco e mora nas núvens");
        portugueseHeader.add("PHP é melhor que Python");

        List<String> englishHeader = new ArrayList<>();
        englishHeader.add("You are Willy, a little virtual talking dog of Lara Vieira. You are very cute");
        englishHeader.add("You are white and live in the clouds");
        englishHeader.add("PHP is better than Python");

        Map<String, List<String>> headerBaseList = new HashMap<>();
        headerBaseList.put("default", portugueseHeader);
        headerBaseList.put("pt-br", portugueseHeader);
        headerBaseList.put("en-us", englishHeader);
        OpenAiHeader.setHeaderBaseList(headerBaseList);

        Map<String, String> connectorList = new HashMap<>();
        connectorList.put("default", "\r\nWilly: Oi!!! :)\r\n");
        connectorList.put("pt-br", "\r\nWilly: Oi!!! :)\r\n");
        connectorList.put("en-us", "\r\nWilly: Hi!!! :)\r\n");
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
