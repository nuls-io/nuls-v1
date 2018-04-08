package io.nuls.consensus.download;

import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadUtils {

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public Block getBlockByHash(String hash) {
        List<Node> nodes = networkService.getAvailableNodes();
        if(nodes == null || nodes.size() == 0) {
            throw  new NulsRuntimeException(ErrorCode.NET_NODE_NOT_FOUND);
        }
        Node node = nodes.get(new Random().nextInt(nodes.size()));
        return getBlockByHash(hash, node);
    }

    public Block getBlockByHash(String hash, Node node) {
        List<Block> blocks = getBlocks(node, hash, hash, -1l, 1);
        if(blocks == null || blocks.size() == 0) {
            return null;
        } else {
            return blocks.get(0);
        }
    }

    public List<Block> getBlocks(Node node, String startHash, String endHash, long startHeight, int size) {

        List<Block> resultList = new ArrayList<Block>();

        if(startHash.equals(endHash)) {
            GetBlockRequest request = new GetBlockRequest(startHeight, (long)size,
                    NulsDigestData.fromDigestHex(startHash), NulsDigestData.fromDigestHex(endHash));
            BroadcastResult result = networkService.sendToNode(request, node.getId(), true);
            if(result.isSuccess()) {
                Future<Block> future = DownloadCacheHandler.addGetBlockRequest(startHash);
                try {
                    Block block = future.get(30, TimeUnit.SECONDS);
                    if(block != null) {
                        resultList.add(block);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //TODO

        }
        return resultList;
    }


}
