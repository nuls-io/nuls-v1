package io.nuls.core.event;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.NulsModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public class EventManager {
    private static final Map<String, Class<? extends NulsEvent>> eventMap = new HashMap<>();
    private static final List<Object[]> tempList = new ArrayList<>();

    public static void isLegal(Class<? extends NulsEvent> eventClass) {
        boolean b = eventMap.values().contains(eventClass);
        if (!b) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "EventClass is not legal:" + eventClass.getName());
        }
    }

    public static void putEvent(short moduleId, short type, Class<? extends NulsEvent> clazz) {
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!");
        }
        if (eventMap.containsKey(moduleId + "_" + type)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type is repeated");
        }
        eventMap.put(moduleId + "_" + type, clazz);
    }

    public static void putEvent(NulsModule module, short type, Class<? extends NulsEvent> clazz) {
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!");
        }
        if (module.getModuleId() != 0) {
            putEvent(module.getModuleId(), type, clazz);
            return;
        }
        Object[] array = new Object[3];
        array[0] = module;
        array[1] = type;
        array[2] = clazz;
        tempList.add(array);
    }

    public static NulsEvent getInstanceByHeader(NulsEventHeader eventHeader) throws IllegalAccessException, InstantiationException {
        if (null == eventHeader || eventHeader.getEventType() == 0) {
            return null;
        }
        Class<? extends NulsEvent> clazz = eventMap.get(eventHeader.getModuleId() + "_" + eventHeader.getEventType());
        NulsEvent event = clazz.newInstance();
        event.setHeader(eventHeader);
        return event;
    }


    public static void refreshEvents() {
        for (Object[] array : tempList) {
            NulsModule module = (NulsModule) array[0];
            short type = (short) array[1];
            Class<? extends NulsEvent> clazz = (Class<? extends NulsEvent>) array[2];
            putEvent(module.getModuleId(), type, clazz);
        }
        tempList.clear();
    }
}
