package io.nuls.event.bus.processor.thread;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.handler.intf.NulsEventHandler;

/**
 *
 * @author Niels
 * @date 2017/11/6
 *
 */
public class NulsEventCall<T extends BaseNulsEvent> implements Runnable {
    private final T event;
    private final NulsEventHandler<T> handler;

    public NulsEventCall(T event, NulsEventHandler<T> handler) {
        this.event = event;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (null == event || null == handler) {
            return ;
        }
        try {
            boolean ok = handler.getFilterChain().startDoFilter(event);
            if (ok) {
                handler.onEvent(event);
            }
        }catch (Exception e){
            Log.error(e);
        }
        return ;
    }
}
