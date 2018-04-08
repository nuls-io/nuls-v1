package io.nuls.consensus.download;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.network.entity.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private DownloadUtils downloadUtils = new DownloadUtils();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private NetworkNewestBlockInfos newestInfos;
    private QueueService<Block> blockQueue;
    private String queueName;

    private int maxDowncount = 100;

    public DownloadThreadManager(NetworkNewestBlockInfos newestInfos, QueueService<Block> blockQueue, String queueName) {
        this.newestInfos = newestInfos;
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {

        System.out.println("============================");
        System.out.println(newestInfos);
        System.out.println("============================");

        boolean isContinue = checkFirstBlock();

        if(!isContinue) {
            return true;
        }

        List<Node> nodes = newestInfos.getNodes();
        String netBestHash = newestInfos.getNetBestHash();
        long netBestHeight = newestInfos.getNetBestHeight();

        Block localBestBlock = blockService.getBestBlock();
        String localBestHash = localBestBlock.getHeader().getHash().getDigestHex();
        long localBestHeight = localBestBlock.getHeader().getHeight();

        ThreadPoolExecutor executor = TaskManager.createThreadPool(nodes.size(), 0,
                new NulsThreadFactory(NulsConstant.MODULE_ID_CONSENSUS, "download-thread"));

        List<FutureTask<ResultMessage>> futures = new ArrayList<>();

        long totalCount = netBestHeight - localBestHeight;

        long laveCount = totalCount;

        long downCount = (long) Math.ceil((double) totalCount / maxDowncount);

        for(long i = 0 ; i < downCount ; i++) {

            long startHeight = (localBestHeight + 1) + i * maxDowncount;

            for(int j = 0 ; j < nodes.size() ; j ++) {

                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                if(start + size > netBestHeight) {
                    size = (int) (netBestHeight - start) + 1;
                }

                DownloadThread downloadThread = new DownloadThread(localBestHash, netBestHash, start, size, nodes.get(j));

                FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<ResultMessage>(downloadThread);
                executor.execute(new Thread(downloadThreadFuture));

                futures.add(downloadThreadFuture);
            }
        }

        for (FutureTask<ResultMessage> task : futures) {
            ResultMessage result = task.get();
            for(Block block : result.getBlockList()) {
                blockQueue.offer(queueName, block);
            }
        }
        executor.shutdown();

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
//        checkRollback(localBestBlock);
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
