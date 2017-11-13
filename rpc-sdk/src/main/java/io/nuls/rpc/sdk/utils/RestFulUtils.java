package io.nuls.rpc.sdk.utils;

import io.nuls.rpc.sdk.entity.RpcClientResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by Niels on 2017/10/31.
 * nuls.io
 */
public class RestFulUtils {

    private static RestFulUtils instance = new RestFulUtils();

    private String serverUri;

    private RestFulUtils() {
    }

    public void init(String serverUri) {
        this.serverUri = serverUri;
    }


    public static RestFulUtils getInstance() {
        if (null == instance) {
            throw new RuntimeException("RestFulUtils hasn't inited yet!");
        }
        return instance;
    }

    private Client client = ClientBuilder.newClient();

    public RpcClientResult get(String path, Map<String, String> params) {
        if (null == serverUri) {
            throw new RuntimeException("service url is null");
        }
        WebTarget target = client.target(serverUri).path(path);
        if (null != params && !params.isEmpty()) {
            for (String key : params.keySet()) {
                target.queryParam(key, params.get(key));
            }
        }
        return target.request(APPLICATION_JSON).get(RpcClientResult.class);
    }

    public RpcClientResult post(String path, String content) {
        if (null == serverUri) {
            throw new RuntimeException("service url is null");
        }
        WebTarget target = client.target(serverUri).path(path);
        return target.request().buildPost(Entity.entity(content, MediaType.APPLICATION_JSON)).invoke(RpcClientResult.class);
    }

    public RpcClientResult put(String path, String content) {
        if (null == serverUri) {
            throw new RuntimeException("service url is null");
        }
        WebTarget target = client.target(serverUri).path(path);
        return target.request().buildPut(Entity.entity(content, MediaType.APPLICATION_JSON)).invoke(RpcClientResult.class);
    }

    public RpcClientResult delete(String path, Map<String, String> params) {
        if (null == serverUri) {
            throw new RuntimeException("service url is null");
        }
        WebTarget target = client.target(serverUri).path(path);
        if (null != params && !params.isEmpty()) {
            for (String key : params.keySet()) {
                target.queryParam(key, params.get(key));
            }
        }
        return target.request().delete(RpcClientResult.class);
    }

}
