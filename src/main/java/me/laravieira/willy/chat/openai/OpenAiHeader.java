package me.laravieira.willy.chat.openai;

import me.laravieira.willy.storage.ContextStorage;

import java.util.*;

public class OpenAiHeader {
    private static final Map<String, List<String>> HEADER_BASE_LIST = new HashMap<>();
    private static final Map<String, String> CONNECTOR_LIST = new HashMap<>();

    private final List<String> header = new ArrayList<>();
    private final String connector;
    private final UUID context;

    public static void setHeaderBaseList(Map<String, List<String>> headerBaseList) {
        HEADER_BASE_LIST.putAll(headerBaseList);
    }

    public static void setConnectorList(Map<String, String> connectorList) {
        CONNECTOR_LIST.putAll(connectorList);
    }

    OpenAiHeader(UUID context) {
        String language = ContextStorage.of(context).getLanguage();
        this.context = context;
        this.header.addAll(HEADER_BASE_LIST.getOrDefault(language, HEADER_BASE_LIST.get("default")));
        this.connector = CONNECTOR_LIST.getOrDefault(language, CONNECTOR_LIST.get("default"));
        setApp(ContextStorage.of(context).getApp());
    }

    public String build() {
        StringBuilder header = new StringBuilder();
        for(int i = 0; i < this.header.size(); i++)
            header.append(i + 1).append(". ").append(this.header.get(i)).append(";\r\n");
        header.append(connector);
        return header.toString();
    }

    public void add(String header) {
        this.header.add(header);
    }

    public void setApp(String app) {
        String language = ContextStorage.of(context).getLanguage();
        if(Objects.equals(language, "en-us"))
            this.header.add("Through "+app);
        else
            this.header.add("Pelo "+app);
    }
}
