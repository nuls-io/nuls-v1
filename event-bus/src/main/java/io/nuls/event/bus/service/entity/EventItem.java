/*
 *
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

package io.nuls.event.bus.service.entity;


import io.nuls.protocol.event.base.BaseEvent;

/**
 * @author Niels
 * @date 2018/3/9
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
