package me.laravieira.willy.internal;

public interface WillyChat {
    void connect();
    void disconnect();
    boolean isConnected();
    void refresh();
}
