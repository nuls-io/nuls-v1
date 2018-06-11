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

import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ln on 2018-05-23.
 */
public final class HandlerManager<M extends BaseMessage, H extends NulsMessageHandler<? extends BaseMessage>> {

    private final Map<Class, Set<String>> messageHandlerMapping = new HashMap<>();
    private final Map<String, H> handlerMap = new HashMap<>();

    private static final HandlerManager INSTANCE = new HandlerManager();

    public static HandlerManager getInstance() {
        return INSTANCE;
    }

    private HandlerManager() {
    }

    public String registerMessageHandler(String handlerId, Class<M> messageClass, H handler) {
        AssertUtil.canNotEmpty(messageClass, "registerMessageHandler faild");
        AssertUtil.canNotEmpty(handler, "registerMessageHandler faild");
        if (StringUtils.isBlank(handlerId)) {
            handlerId = StringUtils.getNewUUID();
        }
        handlerMap.put(handlerId, handler);
        cacheHandlerMapping(messageClass, handlerId);
        return handlerId;
    }

    private void cacheHandlerMapping(Class<M> messageClass, String handlerId) {

        Set<String> ids = messageHandlerMapping.get(messageClass);
        if (null == ids) {
            ids = new HashSet<>();
        }
        ids.add(handlerId);
        messageHandlerMapping.put(messageClass, ids);
    }

    public Set<NulsMessageHandler> getHandlerList(Class<M> clazz) {
        Set<String> ids = messageHandlerMapping.get(clazz);
        Set<NulsMessageHandler> set = new HashSet<>();
        do {
            if (null == ids || ids.isEmpty()) {
                break;
            }
            for (String id : ids) {
                if (StringUtils.isBlank(id)) {
                    continue;
                }
                NulsMessageHandler handler = handlerMap.get(id);
                if (null == handler) {
                    continue;
                }
                set.add(handler);
            }
        } while (false);
        if (!clazz.equals(BaseMessage.class)) {
            set.addAll(getHandlerList((Class<M>) clazz.getSuperclass()));
        }
        return set;
    }

    public void removeMessageHandler(String handlerId) {
        handlerMap.remove(handlerId);
    }
}
