package io.nuls.rpcserver.aop;
import org.glassfish.grizzly.http.server.Request;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class HttpContextHelper {

    private static final Map<String,Request> map = new ConcurrentHashMap<>();

    public static void put(Request request){
        map.put(Thread.currentThread().getName(),request);
    }

    public static Request getRequest(){
        return map.get(Thread.currentThread().getName());
    }
}
