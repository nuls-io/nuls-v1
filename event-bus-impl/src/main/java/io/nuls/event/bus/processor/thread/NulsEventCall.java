package io.nuls.event.bus.processor.thread;

import io.nuls.core.event.NulsEvent;
import io.nuls.event.bus.event.handler.NulsEventHandler;

import java.util.concurrent.Callable;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public class NulsEventCall<T extends NulsEvent> implements Callable<T> {
    private final T event;
    private final NulsEventHandler<T> handler;

    public NulsEventCall(T event, NulsEventHandler<T> handler) {
        this.event = event;
        this.handler = handler;
    }

    @Override
    public T call() throws Exception {
        if (null == event || null == handler) {
            return null;
        }
        boolean ok = handler.getFilterChain().startDoFilter(event);
        if (ok) {
            handler.onEvent(event);
        }
        return null;
    }
}
