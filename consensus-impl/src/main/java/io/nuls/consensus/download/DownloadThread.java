package io.nuls.consensus.download;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThread implements Callable<ResultMessage> {

    private DownloadUtils downloadUtils = new DownloadUtils();

    private String startHash;
    private String endHash;
    private long startHeight;
    private int size;
    private Node node;

    public DownloadThread(String startHash, String endHash, long startHeight, int size, Node node) {
        this.startHash = startHash;
        this.endHash = endHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
    }

    @Override
    public ResultMessage call() throws Exception {

        Log.info("================== download thread : " + Thread.currentThread().getName() + " ,  startHeight : " + startHeight + ", size : " + size + " , from node : " + node.getId() + " , startHash : " + startHash + " , endHash : " + endHash);
        List<Block> blockList = downloadUtils.getBlocks(node, startHash, endHash, startHeight, size);
        Log.info("================== download complete thread : " + Thread.currentThread().getName() + " ,  startHeight : " + startHeight + ", size : " + size + " , from node : " + node.getId());

        return new ResultMessage(startHash, endHash, startHeight, size, node, blockList);
    }
}
