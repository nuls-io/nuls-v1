/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
 *
 * @author Niels
 * @date 2017/10/31
 *
 */
public class RestFulUtils {

    private static RestFulUtils instance = new RestFulUtils();

    private String serverUri;

    private RestFulUtils() {
    }

    public void setServerUri(String serverUri) {
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
                target = target.queryParam(key, params.get(key));
            }
        }
        return target.request(APPLICATION_JSON).get(RpcClientResult.class);
    }

    public RpcClientResult post(String path, Map<String, String> paramsMap) {
        try {
            return post(path, JSONUtils.obj2json(paramsMap));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

//    public RpcClientResult delete(String path, Map<String, String> params) {
//        if (null == serverUri) {
//            throw new RuntimeException("service url is null");
//        }
//        WebTarget target = client.target(serverUri).path(path);
//        if (null != params && !params.isEmpty()) {
//            for (String key : params.keySet()) {
//                target = target.queryParam(key, params.get(key));
//            }
//        }
//        return target.request().delete(RpcClientResult.class);
//    }

}
