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

package io.nuls.notify.handler;

import io.nuls.notify.controller.WebSocketDelegate;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * @author daviyang35
 * @date 2018/2/25
 */
public class WebSocketHandler extends WebSocketServer {
    public WebSocketDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(WebSocketDelegate delegate) {
        this.delegate = delegate;
    }

    private WebSocketDelegate delegate;

    public WebSocketHandler(short listenPort) {
        super(new InetSocketAddress("127.0.0.1", listenPort));
        this.setReuseAddr(true);
        this.setTcpNoDelay(true);
        this.setConnectionLostTimeout(15);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        if (delegate != null) {
            delegate.onConnected(webSocket);
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        if (delegate != null) {
            delegate.onDisconnected(webSocket);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        if (delegate != null) {
            delegate.onMessage(webSocket, s);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {

    }
}
