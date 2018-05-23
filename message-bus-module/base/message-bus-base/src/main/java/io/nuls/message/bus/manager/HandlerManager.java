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
