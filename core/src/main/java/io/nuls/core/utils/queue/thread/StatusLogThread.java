package io.nuls.core.utils.queue.thread;

import io.nuls.core.utils.queue.manager.QueueManager;

/**
 * Created by Niels on 2017/11/15.
 */
public class StatusLogThread implements Runnable {
    @Override
    public void run() {
        QueueManager.logQueueStatus();
    }
}
