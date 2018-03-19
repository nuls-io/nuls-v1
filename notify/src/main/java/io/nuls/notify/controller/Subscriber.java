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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @author daviyang35
 * @date 2018/3/16
 */
public class Subscriber {
    WeakReference<WebSocket> socket;
    Boolean monitor;

    public WebSocket getSocket() {
        return socket.get();
    }

    public void setSocket(WebSocket socket) {
        this.socket = new WeakReference<>(socket);
    }

    Set<Integer> moduleSet = new HashSet<>();
    Set<String> eventSet = new HashSet<>();

    public void subscribeAsMonitor(Boolean isMonitor) {
        this.monitor = isMonitor;
        if (this.monitor) {
            moduleSet.clear();
            eventSet.clear();
        }
    }

    public void subscribeModule(Integer moduleID, Boolean isSubscrib) {
        if (this.monitor) {
            return;
        }

        if (isSubscrib) {
            moduleSet.add(moduleID);

        } else {
            moduleSet.remove(moduleID);
        }
    }

    public void subscribeEvent(String eventName, Boolean isSubscrib) {
        if (this.monitor) {
            return;
        }

        if (isSubscrib) {
            eventSet.add(eventName);

        } else {
            eventSet.remove(eventName);
        }
    }

    public Boolean isSubscribed(short moduleID, String eventName) {
        if (this.monitor) {
            return true;
        }

        if (moduleSet.contains(moduleID)) {
            return true;
        }

        if (eventSet.contains(eventName)) {
            return true;
        }

        return false;
    }
}
