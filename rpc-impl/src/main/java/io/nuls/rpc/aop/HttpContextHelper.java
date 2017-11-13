package io.nuls.rpc.aop;

import org.glassfish.grizzly.http.server.Request;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class HttpContextHelper {

    private static final ThreadLocal<Request> map = new ThreadLocal<>();

    public static void put(Request request) {
        map.set(request);
    }

    public static Request getRequest() {
        return map.get();
    }
}
