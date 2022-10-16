package me.laravieira.willy.chat.bloom;

import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class Bloom implements WillyChat {
    public static final Path     BLOOM_PATH = new File("src/main/python/me.laravieira.willy/Bloom.py").toPath();
    private static boolean       ENABLED = false;
    private static final String  TOKEN = Config.getString("bloom.token");

    private static final int     MAX_LENGTH = 32;
    private static final Double  TOP_K = .0;
    private static final int     BEAMS = 0;
    private static final int     NO_REPEAT = 2;
    private static final Double  TOP_P = .9;
    private static final Double  TEMPERATURE = .7;
    private static final int     SEED = 42;
    private static final boolean DECODING = false;
    private static final boolean FULL_TEXT = false;
    public  static final int HISTORY_SIZE = 7;

    public static String[] buildCommand(String content) {
        Map<String, String> args = new HashMap<>();
        List<String> prompt = new ArrayList<>();
        prompt.add("python");
        prompt.add(BLOOM_PATH.toString());

        args.put("-m", String.valueOf(MAX_LENGTH));
        args.put("-k", String.valueOf(TOP_K));
        args.put("-b", String.valueOf(BEAMS));
        args.put("-r", String.valueOf(NO_REPEAT));
        args.put("-p", String.valueOf(TOP_P));
        args.put("-t", String.valueOf(TEMPERATURE));
        args.put("-s", String.valueOf(SEED));
        args.put("-d", DECODING ? "True" : "False");
        args.put("-f", FULL_TEXT ? "True" : "False");
        args.put("-a", TOKEN);
        args.put("-i", content);

        args.forEach((key, value) -> {
            prompt.add(key);
            prompt.add(value);
        });

        return prompt.toArray(String[]::new);
    }

    public Bloom() {
        List<String> portugueseHeader = new ArrayList<>();
        portugueseHeader.add("Você é Willy, um cãozinho virtual falante da Lara Vieira. Você é muito fofo");
        portugueseHeader.add("Vocé é branco e mora nas núvens");

        List<String> englishHeader = new ArrayList<>();
        englishHeader.add("You are Willy, a little virtual talking dog of Lara Vieira. You are very cute");
        portugueseHeader.add("You are white and live in the clouds");

        Map<String, List<String>> headerBaseList = new HashMap<>();
        headerBaseList.put("default", portugueseHeader);
        headerBaseList.put("pt-br", portugueseHeader);
        headerBaseList.put("en-us", englishHeader);
        BloomHeader.setHeaderBaseList(headerBaseList);

        Map<String, String> connectorList = new HashMap<>();
        connectorList.put("default", "\r\nWilly: Oi!!! :)\r\n");
        connectorList.put("pt-br", "\r\nWilly: Oi!!! :)\r\n");
        connectorList.put("en-us", "\r\nWilly: Hi!!! :)\r\n");
        BloomHeader.setConnectorList(connectorList);
    }

    @Override
    public void connect() {
        if(Config.getBoolean("bloom.enable"))
            ENABLED = true;
        if(TOKEN == null || TOKEN.length() != 37)
            ENABLED = false;
    }

    @Override
    public void disconnect() {
        ENABLED = false;
    }

    @Override
    public boolean isConnected() {
        return ENABLED;
    }

    @Override
    public void refresh() {

    }
}
