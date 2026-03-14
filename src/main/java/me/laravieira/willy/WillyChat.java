package me.laravieira.willy;

public interface WillyChat {
    void connect();
    void disconnect();
    boolean isConnected();
    void refresh();
    String getName();
}
