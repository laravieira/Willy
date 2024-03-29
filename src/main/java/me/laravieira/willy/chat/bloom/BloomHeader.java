package me.laravieira.willy.chat.bloom;

import me.laravieira.willy.storage.ContextStorage;

import java.util.*;

public class BloomHeader {
    private static final Map<String, List<String>> HEADER_BASE_LIST = new HashMap<>();
    private static final Map<String, String> CONNECTOR_LIST = new HashMap<>();

    private final List<String> header = new ArrayList<>();
    private String connector;

    public static void setHeaderBaseList(Map<String, List<String>> headerBaseList) {
        HEADER_BASE_LIST.putAll(headerBaseList);
    }

    public static void setConnectorList(Map<String, String> connectorList) {
        CONNECTOR_LIST.putAll(connectorList);
    }

    BloomHeader(UUID context) {
        String language = ContextStorage.of(context).getLanguage();
        this.header.addAll(HEADER_BASE_LIST.getOrDefault(language, HEADER_BASE_LIST.get("default")));
        this.connector = CONNECTOR_LIST.getOrDefault(language, CONNECTOR_LIST.get("default"));
    }

    public String build() {
        StringBuilder header = new StringBuilder();
        for(int i = 0; i < this.header.size(); i++)
            header.append(i + 1).append(". ").append(this.header.get(i)).append(";\r\n");
        header.append(connector);
        return header.toString();
    }
}
