package io.nuls.mq.thread;

import io.nuls.mq.manager.QueueManager;

/**
 * Created by Niels on 2017/11/15.
 */
public class StatusLogThread implements Runnable {
    @Override
    public void run() {
        QueueManager.logQueueStatus();
    }
}
