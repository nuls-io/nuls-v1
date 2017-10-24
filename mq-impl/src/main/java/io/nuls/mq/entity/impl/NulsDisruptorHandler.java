package io.nuls.mq.entity.impl;

import com.lmax.disruptor.EventHandler;
import io.nuls.mq.entity.StatInfo;

/**
 * Created by Niels on 2017/10/11.
 * nuls.io
 */
public class NulsDisruptorHandler<T> implements EventHandler<T> {

    private final EventHandler<T> handler;
    private final StatInfo statInfo;

    public NulsDisruptorHandler(EventHandler<T> handler, StatInfo statInfo){
        this.handler = handler;
        this.statInfo = statInfo;
    }
    @Override
    public void onEvent(T t, long l, boolean b) throws Exception {
        handler.onEvent(t,l,b);
        statInfo.takeOne();
    }
}
