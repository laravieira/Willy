package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Willy;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;

import java.util.EventListener;

public class Handler implements org.eclipse.jetty.server.Handler {
    public static final JSONObject DEFAULT_RESPONSE = new JSONObject() {{
        put("name", Willy.getWilly().getName());
        put("version", Willy.getWilly().getFullVersion());
        put("paths", new String[] {"/ping", "/status"});
    }};

    private boolean toMethod(Controller controller) {
        return switch (controller.request.getMethod()) {
            case "GET" -> controller.onGet();
            case "POST" -> controller.onPost();
            case "PUT" -> controller.onPut();
            case "DELETE" -> controller.onDelete();
            case "HEAD" -> controller.onHead();
            case "OPTIONS" -> controller.onOptions();
            default -> false;
        };
    }

    private boolean onNoMethod(Controller controller) {
        if(toMethod(controller))
            return true;
        return controller.toJSON(DEFAULT_RESPONSE, 405);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (request.getHttpURI().getPath().equals("/ping"))
            return onNoMethod(new Ping(request, response, callback));
        if (request.getHttpURI().getPath().equals("/status"))
            return onNoMethod(new Status(request, response, callback));
        return new Controller(request, response, callback).toJSON(DEFAULT_RESPONSE, 404);
    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public void setServer(Server server) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStarting() {
        return false;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public boolean addEventListener(EventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeEventListener(EventListener eventListener) {
        return false;
    }
}
