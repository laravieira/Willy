package me.laravieira.willy.context;

import java.io.File;

public interface SenderInterface {
    void send(Object message);
    void sendText(String message);
    void sendLink(Message message);
    void sendStick(Message message);
    void sendGif(Message message);
    void sendImage(Message message);
    void sendVideo(Message message);
    void sendAudio(Message message);
    void sendLocation(Message message);
    void sendContact(Message message);
    void sendFile(File message);
}
