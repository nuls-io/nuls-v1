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

package io.nuls.message.bus.manager;

import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/5/12
 */
public class MessageManager {

    private static final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = new HashMap<>();

    private static void putMessage(short moduleId, int type, Class<? extends BaseMessage> msgClass) {
        MESSAGE_MAP.put(moduleId + "_" + type, msgClass);
    }

    public static Class<? extends BaseMessage> getMessage(short moduleId, int type) {
        return MESSAGE_MAP.get(moduleId + "_" + type);
    }

    public static void putMessage(Class<? extends BaseMessage> msgClass) {
        try {
            BaseMessage message = msgClass.newInstance();
            putMessage(message.getHeader().getModuleId(), message.getHeader().getMsgType(), msgClass);
        } catch (Exception e) {
            //do nothing
        }
    }
}
