/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.protocol.base.download;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.GetBlockRequest;
import io.nuls.protocol.event.GetBlocksHashRequest;
import io.nuls.protocol.event.entity.BlockHashResponse;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.NulsDigestData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadUtils {

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public Block getBlockByHash(long height, String hash) {
        Collection<Node> nodeList = networkService.getAvailableNodes();
        List<Node> nodes = new ArrayList<>(nodeList);
        if (nodes == null) {
            throw new NulsRuntimeException(ErrorCode.NET_NODE_NOT_FOUND);
        }
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            if (node.getVersionMessage().getBestBlockHeight() < height) {
                nodes.remove(i);
            } else if (height == node.getVersionMessage().getBestBlockHeight() && !node.getVersionMessage().getBestBlockHash().equals(hash)) {
                nodes.remove(i);
            }
        }
        if(nodes.isEmpty()){
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
