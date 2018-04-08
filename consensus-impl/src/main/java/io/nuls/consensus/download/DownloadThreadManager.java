package io.nuls.consensus.download;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.queue.service.impl.QueueService;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private NetworkNewestBlockInfos newestInfos;
    private QueueService<Block> blockQueue;
    private String queueName;

    public DownloadThreadManager(NetworkNewestBlockInfos newestInfos, QueueService<Block> blockQueue, String queueName) {
        this.newestInfos = newestInfos;
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {

        boolean isContinue = checkFirstBlock();

        if(!isContinue) {
            return false;
        }

        System.out.println("============================");
        System.out.println(newestInfos);
        System.out.println("============================");

        return false;
    }

    private boolean checkFirstBlock() {
        return true;
    }

}
