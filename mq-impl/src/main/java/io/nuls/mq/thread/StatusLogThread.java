package io.nuls.mq.thread;

import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.thread.BaseNulsThread;
import io.nuls.mq.manager.QueueManager;

/**
 * Created by Niels on 2017/11/15.
 */
public class StatusLogThread extends BaseNulsThread {
    public StatusLogThread(BaseNulsModule module, String name) {
        super(module, name);
    }

    @Override
    public void run() {
        QueueManager.logQueueStatus();
    }
}
