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
package io.nuls.protocol.message.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class MessageManager {
    public static final String KEY_SPLIT = "_";
    private static final Map<String, Class<? extends BaseMessage>> EVENT_MAP = new ConcurrentHashMap<>();
    private static final Set<Class<? extends BaseMessage>> EVENT_CLASSES = new HashSet<>();

    public static void care(Class<? extends BaseMessage> busDataClass) {
        boolean b = EVENT_CLASSES.contains(busDataClass);
        if (!b) {
            throw new RuntimeException( "EventClass is not legal:" + busDataClass.getName());
        }
    }

    public static void putEvent(Class<? extends BaseMessage> clazz) {
        try {
            BaseMessage event = clazz.newInstance();
            putEvent(event.getHeader().getModuleId(), event.getHeader().getMsgType(), clazz);
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }
    }

    private static void putEvent(short moduleId, short type, Class<? extends BaseMessage> clazz) {
        if (type == 0) {
            throw new RuntimeException( "the validator type cannot be 0!,module:" + moduleId + ",eventType:" + type);
        }
        EVENT_MAP.put(moduleId + "_" + type, clazz);
        cacheDataClass(clazz);
    }

    private static void cacheDataClass(Class<? extends BaseMessage> clazz) {
        EVENT_CLASSES.add(clazz);
        if (!clazz.getSuperclass().equals(BaseMessage.class)) {
            cacheDataClass((Class<? extends BaseMessage>) clazz.getSuperclass());
        }
    }

//    public static BaseMessage getInstance(byte[] bytes) throws NulsException {
//        MessageHeader header = new MessageHeader();
//
//        header.parse(new NulsByteBuffer(bytes));
//        Class<? extends BaseMessage> clazz = EVENT_MAP.get(header.getModuleId() + "_" + header.getMsgType());
//        if (null == clazz) {
//            return null;
//        }
//        BaseMessage validator;
//        try {
//            validator = clazz.newInstance();
//        } catch (Exception e) {
//            Log.error(e);
//            throw new NulsException(ErrorCode.DATA_PARSE_ERROR);
//        }
//        try {
//            validator.parse(bytes);
//        } catch (Exception e) {
//            Log.error(Arrays.toString(bytes), e);
//            throw e;
//        }
//        return validator;
//    }

    public static Map<String, Class<? extends BaseMessage>> getEventMap() {
        return EVENT_MAP;
    }
}
