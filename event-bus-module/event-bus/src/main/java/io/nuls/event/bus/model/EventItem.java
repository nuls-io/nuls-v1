package io.nuls.event.bus.model;

import io.nuls.protocol.event.base.BaseEvent;

/**
 * @author: Charlie
 * @date: 2018/5/4
 */
public class EventItem {

    private String name;

    private short moduleId;

    private short eventType;

    private Class<? extends BaseEvent> clazz;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    public short getEventType() {
        return eventType;
    }

    public void setEventType(short eventType) {
        this.eventType = eventType;
    }

    public Class<? extends BaseEvent> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends BaseEvent> clazz) {
        this.clazz = clazz;
    }
}
