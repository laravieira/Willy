package me.laravieira.willy.chat.bloom;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class Bloom implements WillyChat {
    public static final Path BLOOM_PATH = new File("src/main/python/me.laravieira.willy/Bloom.py").toPath();
    private static boolean isEnabled = false;

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
            isEnabled = true;
    }

    @Override
    public void disconnect() {
        isEnabled = false;
    }

    @Override
    public boolean isConnected() {
        return isEnabled;
    }

    @Override
    public void refresh() {

    }
}
