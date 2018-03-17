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

package io.nuls.notify.controller.handler;

import io.nuls.core.utils.log.Log;
import io.nuls.notify.controller.RequestHandler;
import io.nuls.notify.controller.Subscriber;
import org.java_websocket.WebSocket;

import java.util.List;
import java.util.Map;

/**
 * @author daviyang35
 * @date 2018/3/11
 */
public class UnsubscribeRequestHandler extends RequestHandler {
    @Override
    public String method() {
        return "unsubscribe";
    }

    @Override
    public Integer handleRequest(WebSocket sock, Map<String, Object> request, Map<String, Object> response) {
        Subscriber subscriber = sock.getAttachment();
        if (subscriber == null) {
            Log.warn("WebSocket didn't hanve subscriber object.");
            return 400;
        }

        if (request.containsKey("monitor")) {
            Boolean monitor = (Boolean) request.get("monitor");
            subscriber.subscribeAsMonitor(monitor);
            return 200;
        }

        List<Integer> moduleArray = (List<Integer>) request.get("module");
        for (Integer moduleID : moduleArray) {
            subscriber.subscribeModule(moduleID,false);
        }

        List<String> eventArray = (List<String>) request.get("event");
        for (String eventName : eventArray) {
            subscriber.subscribeEvent(eventName,false);
        }
        return 200;
    }
}
