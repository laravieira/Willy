package me.laravieira.willy.chat.http;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class Ping extends Controller {
    public Ping(Request request, Response response, Callback callback) {
        super(request, response, callback);
    }

    @Override
    public boolean onGet() {
        JSONObject body = new JSONObject();
        return toJSON(body);
    }
}
