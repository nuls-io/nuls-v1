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

import io.nuls.core.utils.log.Log;
import io.nuls.notify.handler.NulsEventHandler;
import io.nuls.notify.handler.WebSocketHandler;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author daviyang35
 * @date 2018/2/25
 */
public class NotificationController implements WebSocketDelegate, NulsEventDelegate {
    private List<WebSocket> connectionsList = new ArrayList<>();
    private NulsEventHandler nulsEventHandler = new NulsEventHandler();
    private WebSocketHandler webSocketHandler;

    private short listenPort;

    public void start() {
        webSocketHandler = new WebSocketHandler(listenPort);
        webSocketHandler.setDelegate(this);
        webSocketHandler.start();
        nulsEventHandler.setEventDelegate(this);
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
        nulsEventHandler = null;
    }

    @Override
    public void onConnected(WebSocket sock) {
        connectionsList.add(sock);
    }

    @Override
    public void onDisconnected(WebSocket sock) {
        connectionsList.remove(sock);
    }

    @Override
    public void onMessage(WebSocket sock, String msg) {

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
    public void onEventFire() {
        
    }
}
