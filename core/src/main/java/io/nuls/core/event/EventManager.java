package io.nuls.core.event;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;

import java.util.*;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class EventManager {
    private static final Map<String, Class<? extends BaseNulsEvent>> EVENT_MAP = new HashMap<>();
    private static final Set<Class<? extends BaseNulsEvent>> EVENT_CLASSES = new HashSet<>();

    public static void isLegal(Class<? extends BaseNulsEvent> eventClass) {
        boolean b = EVENT_CLASSES.contains(eventClass);
        if (!b) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "EventClass is not legal:" + eventClass.getName());
        }
    }

    public static void putEvent(short moduleId, short type, Class<? extends BaseNulsEvent> clazz) {
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!");
        }
        if (EVENT_MAP.containsKey(moduleId + "_" + type)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type is repeated");
        }
        EVENT_MAP.put(moduleId + "_" + type, clazz);
        cacheEventClass(clazz);
    }

    private static void cacheEventClass(Class<? extends BaseNulsEvent> clazz) {
        EVENT_CLASSES.add(clazz);
        if(!clazz.getSuperclass().equals(BaseNulsEvent.class)){
            cacheEventClass((Class<? extends BaseNulsEvent>) clazz.getSuperclass());
        }
    }

    public static void putEvent(BaseNulsModule module, short type, Class<? extends BaseNulsEvent> clazz) {
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!");
        }
        putEvent(module.getModuleId(), type, clazz);
    }

    public static BaseNulsEvent getInstanceByHeader(NulsEventHeader eventHeader) throws IllegalAccessException, InstantiationException {
        if (null == eventHeader || eventHeader.getEventType() == 0) {
            return null;
        }
        Class<? extends BaseNulsEvent> clazz = EVENT_MAP.get(eventHeader.getModuleId() + "_" + eventHeader.getEventType());
        BaseNulsEvent event = clazz.newInstance();
        event.setHeader(eventHeader);
        return event;
    }
}
