package io.nuls.core.event;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
public class EventManager {
    private static final Map<String, Class<? extends BaseNulsEvent>> EVENT_MAP = new HashMap<>();
    private static final List<Object[]> TEMP_LIST = new ArrayList<>();

    public static void isLegal(Class<? extends BaseNulsEvent> eventClass) {
        boolean b = EVENT_MAP.values().contains(eventClass);
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
    }

    public static void putEvent(BaseNulsModule module, short type, Class<? extends BaseNulsEvent> clazz) {
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
        TEMP_LIST.add(array);
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


    public static void refreshEvents() {
        for (int i = TEMP_LIST.size() - 1; i >= 0; i--) {
            Object[] array = TEMP_LIST.get(i);
            BaseNulsModule module = (BaseNulsModule) array[0];
            if (0 == module.getModuleId()) {
                continue;
            }
            short type = (short) array[1];
            Class<? extends BaseNulsEvent> clazz = (Class<? extends BaseNulsEvent>) array[2];
            putEvent(module.getModuleId(), type, clazz);
            TEMP_LIST.remove(i);
        }
    }
}
