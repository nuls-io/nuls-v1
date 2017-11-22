package io.nuls.rpc.aop;

import org.glassfish.grizzly.http.server.Request;

/**
 * @author Niels
 * @date 2017/9/28
 */
public class HttpContextHelper {

    private static final ThreadLocal<Request> LOCAL = new ThreadLocal<>();

    public static void put(Request request) {
        LOCAL.set(request);
    }

    public static Request getRequest() {
        return LOCAL.get();
    }

    public static void removeRequest() {
        LOCAL.remove();
    }
}
