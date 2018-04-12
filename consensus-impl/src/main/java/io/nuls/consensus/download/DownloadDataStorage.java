package io.nuls.consensus.download;

import io.nuls.consensus.manager.BlockManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.service.impl.QueueService;

import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadDataStorage implements Callable<Boolean> {

    private QueueService<Block> blockQueue;
    private String queueName;
    private boolean running = true;

    public DownloadDataStorage(QueueService<Block> blockQueue, String queueName) {
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            Block block;
            while((block = blockQueue.take(queueName)) != null) {
                if(block.getHeader() == null) {
                    break;
                }
                BlockManager.getInstance().addBlock(block, true, null);
            }
            return true;
        } catch (InterruptedException e) {
            Log.error(e);
            return false;
        }
    }

}
