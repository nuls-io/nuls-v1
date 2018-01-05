package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNetworkEvent<T extends BaseNulsData> extends BaseEvent<T> {
    public BaseNetworkEvent(short moduleId, short eventType) {
        super(moduleId, eventType);
    }
}
