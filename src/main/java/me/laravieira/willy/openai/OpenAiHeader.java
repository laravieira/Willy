package me.laravieira.willy.openai;

import me.laravieira.willy.kernel.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAiHeader {
    private static final Map<String, List<String>> HEADER_BASE_LIST = new HashMap<>();
    private static final Map<String, String> CONNECTOR_LIST = new HashMap<>();

    private List<String> header = new ArrayList<>();
    private String connector = null;

    public static void setHeaderBaseList(Map<String, List<String>> headerBaseList) {
        HEADER_BASE_LIST.putAll(headerBaseList);
    }

    public static void setConnectorList(Map<String, String> connectorList) {
        CONNECTOR_LIST.putAll(connectorList);
    }

    OpenAiHeader() {
        this.header.addAll(HEADER_BASE_LIST.get("default"));
        this.connector = CONNECTOR_LIST.get("default");
    }

    OpenAiHeader(Context context) {
        this.header.addAll(HEADER_BASE_LIST.getOrDefault(context.getUserLanguage(), HEADER_BASE_LIST.get("default")));
        this.connector = CONNECTOR_LIST.getOrDefault(context.getUserLanguage(), CONNECTOR_LIST.get("default"));
    }

    public String build() {
        StringBuilder header = new StringBuilder();
        for(int i = 0; i < this.header.size(); i++)
            header.append(i + 1).append(". ").append(this.header.get(i)).append(";\r\n");
        header.append(connector);
        return header.toString();
    }
}
