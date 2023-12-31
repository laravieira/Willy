package me.laravieira.willy.chat.http;

public interface HandlerHTTP {
    boolean onGet() throws Exception;
    boolean onPost() throws Exception;
    boolean onPut() throws Exception;
    boolean onDelete() throws Exception;
    boolean onHead() throws Exception;
    boolean onOptions() throws Exception;
}
