/*
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
 *
 */

package io.nuls.notify.controller;

import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.notify.controller.handler.QueryRequestHandler;
import io.nuls.notify.controller.handler.SubscribeRequestHandler;
import io.nuls.notify.controller.handler.UnsubscribeRequestHandler;
import io.nuls.notify.handler.NulsEventHandler;
import io.nuls.notify.handler.WebSocketHandler;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author daviyang35
 * @date 2018/2/25
 */
public class NotificationController implements WebSocketDelegate, NulsEventDelegate {

    private Map<String, RequestHandler> dispatcherMap = new HashMap<>();
    private WebSocketHandler webSocketHandler;
    private NulsEventHandler nulsEventHandler = new NulsEventHandler();
    private Map<WebSocket, Subscriber> subscriberMap = new HashMap<>();
    private short listenPort;

    public NotificationController() {
        this.addRequestHandler(new QueryRequestHandler());
        this.addRequestHandler(new SubscribeRequestHandler());
        this.addRequestHandler(new UnsubscribeRequestHandler());
    }

    private void addRequestHandler(RequestHandler handler) {
        String method = handler.method().toLowerCase();
        if (!dispatcherMap.containsKey(method)) {
            dispatcherMap.put(method, handler);
        } else {
            Log.error("Duplicate request method handler. {} {}", method, handler.getClass().getName());
        }
    }

    public void start() {
        nulsEventHandler.setEventDelegate(this);
        webSocketHandler = new WebSocketHandler(listenPort);
        webSocketHandler.setDelegate(this);
        webSocketHandler.start();
        Log.info("Notify module running on port:{}", listenPort);
    }

    public void stop() {
        // stop web socket server
        try {
            webSocketHandler.stop();
        } catch (IOException | InterruptedException e) {
           Log.error(e);
        }

        webSocketHandler = null;
        nulsEventHandler.setEventDelegate(null);
    }

    @Override
    public void onConnected(WebSocket sock) {
        synchronized (subscriberMap) {
            Subscriber subscriber = new Subscriber();
            subscriber.setSocket(sock);
            sock.setAttachment(subscriber);
            subscriberMap.put(sock, subscriber);
        }
    }

    @Override
    public void onDisconnected(WebSocket sock) {
        sock.setAttachment(null);
        synchronized (subscriberMap) {
            subscriberMap.remove(sock);
        }
    }

    @Override
    public void onMessage(WebSocket sock, String msg) {
        // decode request
        Map<String, Object> request = null;
        Map<String, Object> response = new HashMap<>();
        try {
            request = JSONUtils.json2map(msg);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 400);
        }

        // dispatch handler
        if (request != null) {
            this.handleRequest(sock, request, response);
        }

        // encode response
        try {
            String resp = JSONUtils.obj2json(response);
            sock.send(resp);
        } catch (Exception e) {
            e.printStackTrace();
            Log.debug("Serialized response error.");
        }
    }

    public NulsEventHandler getNulsEventHandler() {
        return nulsEventHandler;
    }

    public short getListenPort() {
        return listenPort;
    }

    public void setListenPort(short listenPort) {
        this.listenPort = listenPort;
    }

    @Override
    public void onEventFire(short moduleID, String eventName, String payload) {
        synchronized (subscriberMap) {
            for (Subscriber subscriber : subscriberMap.values()) {
                if (subscriber.isSubscribed(moduleID, eventName)) {
                    WebSocket sock = subscriber.getSocket();
                    if (sock != null) {
                        sock.send(payload);
                    }
                }
            }
        }
    }

    private void handleRequest(WebSocket sock, Map<String, Object> request, Map<String, Object> response) {
        String method = (String) request.get("method");
        if (method == null) {
            response.put("status", 400);
            return;
        }

        if (!request.containsKey("data")) {
            response.put("status", 400);
            return;
        }

        method = method.toLowerCase();
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        if (dispatcherMap.containsKey(method)) {
            RequestHandler handler = dispatcherMap.get(method);
            Integer status = handler.handleRequest(sock, data, response);
            response.put("status", status);
        } else {
            response.put("status", 404);
        }
    }
}
