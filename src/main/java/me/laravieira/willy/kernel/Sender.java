package me.laravieira.willy.kernel;

import java.awt.*;
import java.io.File;

public class Sender {
    protected Context context;

    public Sender(Context context) {
        this.context = context;
    }

    public void send(String message) {}

    public void sendImage(String message, Image image) {};

    public void sendAudio(String message, Audio audio) {};

    public void sendVideo(String message, Gif gif) {};

    public void sendFile(String message, File file) {};

    public void sendLink(String message, Link link) {};

    public void sendLocation(String message, Location location) {};
}