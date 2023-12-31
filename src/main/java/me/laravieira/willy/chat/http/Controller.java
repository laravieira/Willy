package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;

public class Controller implements HandlerHTTP {
    protected Request request;
    protected Response response;
    protected Callback callback;

    protected boolean toJSON(JSONObject body) {
        return toJSON(body, 200);
    }

    protected boolean toJSON(JSONObject body, int status) {
        response.setStatus(status);
        response.getHeaders().add("Content-Type", "application/json");
        response.write(true, ByteBuffer.wrap(body.toJSONString().getBytes()), callback);
        callback.succeeded();
        return true;
    }

    public Controller(Request request, Response response, Callback callback) {
        this.request  = request;
        this.response = response;
        this.callback = callback;
    }

    @Override
    public boolean onGet() {
        return false;
    }

    @Override
    public boolean onPost() {
        return false;
    }

    @Override
    public boolean onPut() {
        return false;
    }

    @Override
    public boolean onDelete() {
        return false;
    }

    @Override
    public boolean onHead() {
        return false;
    }

    @Override
    public boolean onOptions() {
        return false;
    }
}
