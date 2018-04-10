package io.nuls.consensus.download;

import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.consensus.event.BlocksHashEvent;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.event.GetBlocksHashRequest;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
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
        if (nodes == null || nodes.size() == 0) {
            throw new NulsRuntimeException(ErrorCode.NET_NODE_NOT_FOUND);
        }
        Node node = nodes.get(new Random().nextInt(nodes.size()));
        Block block = getBlockByHash(hash, node);
        if (block == null) {
            BlockLog.debug("get Block failed hash:" + hash + " , form:" + node.getId());
        }
        return block;
    }

    public Block getBlockByHash(String hash, Node node) {
        List<Block> blocks = null;
        try {
            blocks = getBlocks(node, hash, hash, -1L, 1);
        } catch (Exception e) {
            Log.error(e);
        }
        if (blocks == null || blocks.size() == 0) {
            return null;
        } else {
            return blocks.get(0);
        }
    }

    public List<Block> getBlocks(Node node, String startHash, String endHash, long startHeight, int size) throws Exception {

        List<Block> resultList = new ArrayList<Block>();

        if (startHash.equals(endHash)) {
            GetBlockRequest request = new GetBlockRequest(startHeight, (long) size,
                    NulsDigestData.fromDigestHex(startHash), NulsDigestData.fromDigestHex(endHash));
            Future<Block> future = DownloadCacheHandler.addGetBlockRequest(endHash);
            BroadcastResult result = networkService.sendToNode(request, node.getId(), false);
            if (!result.isSuccess()) {
                return resultList;
            }
            try {
                Block block = future.get(30L, TimeUnit.SECONDS);
                if (block != null) {
                    resultList.add(block);
                }
            } catch (Exception e) {
                Log.error(node.getId() + ",start:" + startHeight + " , size:" + size);
                Log.error(e);
                throw e;
            }
        } else {
            GetBlocksHashRequest hashesRequest = new GetBlocksHashRequest(startHeight, size);
            Future<BlockHashResponse> hashesFuture = DownloadCacheHandler.addGetBlockHashesRequest(hashesRequest.getHash().getDigestHex());
            BroadcastResult hashesResult = networkService.sendToNode(hashesRequest, node.getId(), false);
            if (!hashesResult.isSuccess()) {
                return resultList;
            }
            BlockHashResponse response;
            try {
                response = hashesFuture.get(20L, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.error(node.getId() + ",start:" + startHeight + " , size:" + size);
                Log.error(e);
                throw e;
            }
            if (null == response || response.getHashList() == null || response.getHashList().size() != size) {
                Log.warn("get blocks hashList({}-{}) failed:" + node.getId(), startHeight, size);
                return resultList;
            }
            GetBlockRequest request = new GetBlockRequest(startHeight, (long) size,
                    response.getHashList().get(0), response.getBestHash());
            List<Future<Block>> futureList = new ArrayList<>();
            for (NulsDigestData hash : response.getHashList()) {
                Future<Block> future = DownloadCacheHandler.addGetBlockRequest(hash.getDigestHex());
                futureList.add(future);
            }
            BroadcastResult result = networkService.sendToNode(request, node.getId(), false);
            if (!result.isSuccess()) {
                return resultList;
            }
            for (Future<Block> future : futureList) {
                try {
                    Block block = future.get(30L, TimeUnit.SECONDS);
                    if (block != null) {
                        resultList.add(block);
                    } else {
                        return resultList;
                    }
                } catch (Exception e) {
                    Log.error(node.getId() + ",start:" + startHeight + " , size:" + size);
                    Log.error(e);
                    throw e;
                }
            }
        }
        return resultList;
    }


}
