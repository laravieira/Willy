package me.laravieira.willy;

public interface WillyChannel {
    void setContext(Context context);
    void send(WillyMessage message);
    void sendLast();
}
