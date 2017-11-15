package io.nuls.mq.thread;

import io.nuls.core.module.NulsModule;
import io.nuls.core.thread.NulsThread;
import io.nuls.mq.manager.QueueManager;

/**
 * Created by Niels on 2017/11/15.
 */
public class StatusLogThread extends NulsThread {
    public StatusLogThread(NulsModule module, String name) {
        super(module, name);
    }

    @Override
    public void run() {
        QueueManager.logQueueStatus();
    }
}
