package io.nuls.consensus.download;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.DownloadService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.queue.service.impl.QueueService;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThreadManager implements Callable<Boolean> {

    private DownloadUtils downloadUtils = new DownloadUtils();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

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

        try {
            boolean isContinue = checkFirstBlock();

            if (!isContinue) {
                return true;
            }
        } catch (NulsRuntimeException e) {
            return false;
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

        long downCount = (long) Math.ceil((double) totalCount / (maxDowncount * nodes.size()));

        for(long i = 0 ; i < downCount ; i++) {

            long startHeight = (localBestHeight + 1) + i * maxDowncount * nodes.size();

            for(int j = 0 ; j < nodes.size() ; j ++) {

                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                boolean isEnd = false;
                if(start + size >= netBestHeight) {
                    size = (int) (netBestHeight - start) + 1;
                    isEnd = true;
                }

                DownloadThread downloadThread = new DownloadThread(localBestHash, netBestHash, start, size, nodes.get(j));

                FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<ResultMessage>(downloadThread);
                executor.execute(new Thread(downloadThreadFuture));

                futures.add(downloadThreadFuture);

                if(isEnd) {
                    break;
                }
            }
        }

        for (FutureTask<ResultMessage> task : futures) {
            try {
                ResultMessage result = task.get();

                List<Block> blockList = result.getBlockList();
                if (blockList == null || blockList.size() == 0) {
                    blockList = retryDownload(executor, result);
                }

                for(Block block : blockList) {
                    blockQueue.offer(queueName, block);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        return true;
    }

    private List<Block> retryDownload(ThreadPoolExecutor executor, ResultMessage result) throws InterruptedException, java.util.concurrent.ExecutionException {

        //try download to other nodes
        List<Node> otherNodes = new ArrayList<>();

        Node defultNode = result.getNode();

        for(Node node : newestInfos.getNodes()) {
            if(!node.getId().equals(defultNode.getId())) {
                otherNodes.add(node);
            }
        }

        for(Node node : otherNodes) {
            result.setNode(node);
            List<Block> blockList = downloadBlockFromNode(executor, result);
            if(blockList != null && blockList.size() > 0) {
                return blockList;
            }
        }

        //if fail , down again
        result.setNode(defultNode);

        return downloadBlockFromNode(executor, result);
    }

    private List<Block> downloadBlockFromNode(ThreadPoolExecutor executor, ResultMessage result) throws ExecutionException, InterruptedException {
        DownloadThread downloadThread = new DownloadThread(result.getStartHash(), result.getEndHash(), result.getStartHeight(), result.getSize(), result.getNode());

        FutureTask<ResultMessage> downloadThreadFuture = new FutureTask<ResultMessage>(downloadThread);
        executor.execute(new Thread(downloadThreadFuture));

        return downloadThreadFuture.get().getBlockList();
    }

    private boolean checkFirstBlock() throws NulsException {

        Block localBestBlock = blockService.getBestBlock();

        if(localBestBlock.getHeader().getHeight() == 0 || (newestInfos.getNetBestHeight() == localBestBlock.getHeader().getHeight() &&
                newestInfos.getNetBestHash().equals(localBestBlock.getHeader().getHash().getDigestHex()))) {
            return true;
        }

        if(newestInfos.getNetBestHeight() < localBestBlock.getHeader().getHeight()) {
            if(DoubleUtils.div(newestInfos.getNodes().size(), networkService.getAvailableNodes().size(), 2) >= 0.8d && networkService.getAvailableNodes().size() >= networkService.getNetworkParam().maxOutCount()) {
                for (long i = localBestBlock.getHeader().getHeight(); i <= newestInfos.getNetBestHeight(); i--) {
                    blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());
                    localBestBlock = blockService.getBestBlock();
                }
            } else {
                try {
                    Thread.sleep(60*1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resetNetwork();
                return false;
            }
        }

        //check need rollback
        checkRollback(localBestBlock, 0);

        return true;
    }

    private void checkRollback(Block localBestBlock, int rollbackCount) throws NulsException {

        if(rollbackCount >= 10) {
            resetNetwork();
            return;
        }

        List<Node> nodes = newestInfos.getNodes();

        for(Node node : nodes) {
            Block remoteBlock = downloadUtils.getBlockByHash(localBestBlock.getHeader().getHash().getDigestHex(), node);
            if(remoteBlock != null && remoteBlock.getHeader().getHeight() == localBestBlock.getHeader().getHeight()) {
                return;
            }
        }

        if(newestInfos.getNodes().size() >= PocConsensusConstant.ALIVE_MIN_NODE_COUNT) {
            blockService.rollbackBlock(localBestBlock.getHeader().getHash().getDigestHex());
        } else {
            resetNetwork();
            return;
        }

        localBestBlock = blockService.getBestBlock();

        checkRollback(localBestBlock, rollbackCount + 1);
    }

    private void resetNetwork() {
        Map<String, Node> nodesMap = networkService.getNodes();
        for(Map.Entry<String, Node> entry : nodesMap.entrySet()) {
            networkService.removeNode(entry.getValue().getId());
        }
        downloadService.reset();
        throw new NulsRuntimeException(ErrorCode.FAILED);
    }

}
