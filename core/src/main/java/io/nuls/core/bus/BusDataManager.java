package io.nuls.core.bus;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.notice.BaseNulsNotice;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.param.AssertUtil;

import java.util.*;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class BusDataManager {
    private static final Map<String, Class<? extends BaseBusData>> EVENT_MAP = new HashMap<>();
    private static final Set<Class<? extends BaseBusData>> EVENT_CLASSES = new HashSet<>();

    public static void isLegal(Class<? extends BaseBusData> busDataClass) {
        boolean b = EVENT_CLASSES.contains(busDataClass);
        if (!b) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "EventClass is not legal:" + busDataClass.getName());
        }
    }

    public static void putBusData(short moduleId, short type, Class<? extends BaseBusData> clazz) {
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!,module:" + moduleId + ",eventType:" + type);
        }
        if (EVENT_MAP.containsKey(moduleId + "_" + type)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type is repeated,module:" + moduleId + ",eventType:" + type);
        }
        EVENT_MAP.put(moduleId + "_" + type, clazz);
        cacheDataClass(clazz);
    }

    private static void cacheDataClass(Class<? extends BaseBusData> clazz) {
        EVENT_CLASSES.add(clazz);
        if (!clazz.getSuperclass().equals(BaseBusData.class)) {
            cacheDataClass((Class<? extends BaseBusData>) clazz.getSuperclass());
        }
    }

    public static void putBusData(BaseNulsModule module, short type, Class<? extends BaseBusData> clazz) {
        AssertUtil.canNotEmpty(clazz, "event class is null!");
        AssertUtil.canNotEmpty(module, "module is null,message" + clazz.getName());
        if (type == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "the event type cannot be 0!,module:" + module.getModuleId() + ",eventType:" + type);
        }
        putBusData(module.getModuleId(), type, clazz);
    }

    public static BaseNulsEvent getEventInstance(byte[] bytes) throws IllegalAccessException, InstantiationException, NulsException {
        return (BaseNulsEvent) getInstance(bytes);
    }
    public static BaseNulsNotice getNoticeInstance(byte[] bytes) throws IllegalAccessException, InstantiationException, NulsException {
        return (BaseNulsNotice) getInstance(bytes);
    }

    private static BaseBusData getInstance(byte[] bytes) throws IllegalAccessException, InstantiationException, NulsException {
        BusDataHeader header = new BusDataHeader();
        header.parse(new NulsByteBuffer(bytes));
        Class<? extends BaseBusData> clazz = EVENT_MAP.get(header.getModuleId() + "_" + header.getEventType());
        BaseBusData event = clazz.newInstance();
        event.parse(new NulsByteBuffer(bytes));
        return event;
    }

//    public static List<BaseBusData> getInstances(NulsByteBuffer buffer) throws IllegalAccessException, InstantiationException {
//        List<BaseBusData> list = new ArrayList<>();
//        while (!buffer.isFinished()) {
//            BusDataHeader header = new BusDataHeader();
//            header.parse(new NulsByteBuffer(buffer.getPayloadByCursor()));
//            Class<? extends BaseBusData> clazz = EVENT_MAP.get(header.getModuleId() + "_" + header.getEventType());
//            BaseBusData event = clazz.newInstance();
//            event.parse(buffer);
//            list.add(event);
//        }
//        return list;
//    }
}
