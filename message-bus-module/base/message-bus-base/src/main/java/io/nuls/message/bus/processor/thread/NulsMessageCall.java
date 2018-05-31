package io.nuls.message.bus.processor.thread;

import io.nuls.core.tools.log.Log;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.model.ProcessData;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class NulsMessageCall<T extends BaseMessage> implements Runnable {

    private final ProcessData<T> data;
    private final NulsMessageHandler<T> handler;

    public NulsMessageCall(ProcessData<T> data, NulsMessageHandler<T> handler) {
        this.data = data;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (null == data || null == handler) {
            return;
        }
        try {
            long start = System.currentTimeMillis();
            handler.onMessage(data.getData(), data.getNode());
            if(Log.isDebugEnabled()) {
                Log.debug(data.getData().getClass() + ",use:" + (System.currentTimeMillis() - start));
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return;
    }
}
