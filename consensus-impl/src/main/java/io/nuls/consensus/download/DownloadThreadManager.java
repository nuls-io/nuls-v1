package io.nuls.consensus.download;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.network.entity.Node;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private DownloadUtils downloadUtils = new DownloadUtils();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

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
            return true;
        }

        System.out.println("============================");
        System.out.println(newestInfos);
        System.out.println("============================");

        return true;
    }

    private boolean checkFirstBlock() throws NulsException {

        Block localBestBlock = blockService.getBestBlock();

        if(newestInfos.getNetBestHeight() == localBestBlock.getHeader().getHeight() &&
                newestInfos.getNetBestHash().equals(localBestBlock.getHeader().getHash().getDigestHex())) {
            return true;
        }

        if(newestInfos.getNetBestHeight() < localBestBlock.getHeader().getHeight()) {
            for(long i = localBestBlock.getHeader().getHeight() ; i <= newestInfos.getNetBestHeight(); i--) {
                blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());
                localBestBlock = blockService.getBestBlock();
            }
        }

        //check need rollback
        checkRollback(localBestBlock);
        return true;
    }

    private void checkRollback(Block localBestBlock) throws NulsException {
        List<Node> nodes = newestInfos.getNodes();

        for(Node node : nodes) {
            Block remoteBlock = downloadUtils.getBlockByHash(localBestBlock.getHeader().getHash().getDigestHex(), node);
            if(remoteBlock != null && remoteBlock.getHeader().getHeight() == localBestBlock.getHeader().getHeight()) {
                return;
            }
        }

        blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());

        localBestBlock = blockService.getBestBlock();

        checkRollback(localBestBlock);
    }

}
