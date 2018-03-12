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

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author daviyang35
 * @date 2018/3/12
 */
public class SubscriptionContext {
    private Map<Integer, Set<WebSocket>> moduleMap = new HashMap<>();
    private Map<String, Set<WebSocket>> eventMap = new HashMap<>();

    public void subscribeModule(Integer moduleID, WebSocket sock) {
        synchronized (this) {
            Set<WebSocket> moduleSubscriber = moduleMap.get(moduleID);
            if (moduleSubscriber != null) {
                moduleSubscriber.add(sock);
            } else {
                moduleSubscriber = new HashSet<>();
                moduleSubscriber.add(sock);
                moduleMap.put(moduleID, moduleSubscriber);
            }
        }
    }

    public void unsubscribeModule(Integer moduleID, WebSocket sock) {
        synchronized (this) {
            Set<WebSocket> moduleSubscriber = moduleMap.get(moduleID);
            if (moduleSubscriber != null) {
                moduleSubscriber.remove(sock);
            }
        }
    }

    public void subscribeEvent(String eventName, WebSocket sock) {
        synchronized (this) {
            String eventInternalName = eventName.toUpperCase();
            Set<WebSocket> eventSubscriber = eventMap.get(eventInternalName);
            if (eventSubscriber != null) {
                eventSubscriber.add(sock);
            } else {
                eventSubscriber = new HashSet<>();
                eventSubscriber.add(sock);
                eventMap.put(eventInternalName, eventSubscriber);
            }
        }
    }

    public void unsubscribeEvent(String eventName, WebSocket sock) {
        synchronized (this) {
            String eventInternalName = eventName.toUpperCase();
            Set<WebSocket> eventSubscriber = eventMap.get(eventInternalName);
            if (eventSubscriber != null) {
                eventSubscriber.remove(sock);
            }
        }
    }

    public Set<WebSocket> findSubscribedSource(Integer moduleID, String eventName) {
        Set<WebSocket> result = new HashSet<>();
        synchronized (this) {
            // 1st, find out subscribe the moduleID
            Set moduleSubscriber = moduleMap.get(moduleID);
            result.addAll(moduleSubscriber);

            // 2nd, find out subscribe the event
            Set eventSubscriber = eventMap.get(eventName.toLowerCase());
            result.addAll(eventSubscriber);
        }
        return result;
    }

    public void removeSubscriber(WebSocket sock) {
        synchronized (this) {
            for (Set<WebSocket> moduleSubscriber : moduleMap.values()) {
                moduleSubscriber.remove(sock);
            }

            for (Set<WebSocket> moduleSubscriber : eventMap.values()) {
                moduleSubscriber.remove(sock);
            }
        }
    }
}
