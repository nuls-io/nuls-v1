package io.nuls.core.event;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.chain.entity.BaseNulsData;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseLocalEvent<T extends BaseNulsData> extends BaseEvent<T> {


    public BaseLocalEvent(short moduleId, short eventType) {
        super(moduleId, eventType);
    }
}
