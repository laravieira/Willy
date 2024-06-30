package me.laravieira.willy.context;

import java.io.File;

public interface SenderInterface {
    void send(Object message);
    void sendText(String message);
    void sendLink(Message message) throws Exception;
    void sendStick(Message message) throws Exception;
    void sendGif(Message message) throws Exception;
    void sendImage(Message message) throws Exception;
    void sendVideo(Message message) throws Exception;
    void sendAudio(Message message) throws Exception;
    void sendLocation(Message message) throws Exception;
    void sendContact(Message message) throws Exception;
    void sendFile(File message) throws Exception;
}
