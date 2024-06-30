package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSON;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class Ping extends Controller {
    public Ping(Request request, Response response, Callback callback) {
        super(request, response, callback);
    }

    @Override
    public boolean onGet() {
        Object body = JSON.toJSON("ping");
        return toJSON(body);
    }
}
