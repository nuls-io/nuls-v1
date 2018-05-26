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
 *
 */

package io.nuls.protocol.base.download.utils;

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.download.cache.DownloadCacheHandler;
import io.nuls.protocol.message.*;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.GetTxGroupParam;
import io.nuls.protocol.model.TxGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadUtils {

    private static MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    public static Block getBlockByHash(NulsDigestData hash, Node node) {
        if(hash == null || node == null) {
            return null;
        }
        GetBlockMessage message = new GetBlockMessage(hash);
        Future<Block> future = DownloadCacheHandler.addGetBlockRequest(hash);
        Result result = messageBusService.sendToNode(message, node, false);
        if (!result.isSuccess()) {
            DownloadCacheHandler.removeBlockFuture(hash);
            return null;
        }
        try {
            Block block = future.get(30L, TimeUnit.SECONDS);
            return block;
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        } finally {
            DownloadCacheHandler.removeBlockFuture(hash);
        }
    }

    public static List<Block> getBlocks(Node node, long startHeight, long endHeight) throws Exception {

        List<Block> resultList = new ArrayList<>();

        if(node == null || startHeight < 0L || startHeight > endHeight) {
            return resultList;
        }

        Log.info("download block " + startHeight + " , " + endHeight + " from : " + node.getId());

        GetBlocksByHeightMessage message = new GetBlocksByHeightMessage(startHeight, endHeight);

        NulsDigestData requestHash = null;
        try {
            requestHash = NulsDigestData.calcDigestData(message.getMsgBody().serialize());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Future<CompleteParam> taskFuture = DownloadCacheHandler.addTaskRequest(requestHash);

        List<Map<NulsDigestData, Future<Block>>> blockFutures = new ArrayList<>();
        for(long i = startHeight ; i <= endHeight ; i++) {
            NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(i));
            Future<Block> blockFuture = DownloadCacheHandler.addGetBlockRequest(hash);

            Map<NulsDigestData, Future<Block>> blockFutureMap = new HashMap<>();
            blockFutureMap.put(hash, blockFuture);
            blockFutures.add(blockFutureMap);
        }

        Result result = messageBusService.sendToNode(message, node, false);
        if (!result.isSuccess()) {
            DownloadCacheHandler.removeTaskFuture(message.getHash());

            for(Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                    DownloadCacheHandler.removeBlockFuture(entry.getKey());
                }
            }
            return resultList;
        }

        try {
            CompleteParam taskResult = taskFuture.get();
            if(taskResult.isSuccess()) {
                for(Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                    for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                        Block block = entry.getValue().get(30L, TimeUnit.SECONDS);
                        resultList.add(block);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(node.getId() + ",start:" + startHeight + " , endHeight:" + endHeight);
            Log.error(e.getMessage());
            return new ArrayList<>();
        } finally {
            DownloadCacheHandler.removeTaskFuture(requestHash);

            for(Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                    DownloadCacheHandler.removeBlockFuture(entry.getKey());
                }
            }
        }
        return resultList;
    }

    public static List<NulsDigestData> getBlocksHash(Node node, long startHeight, long endHeight) {

        if(node == null || startHeight < 0L || endHeight < 0L || startHeight > endHeight) {
            return new ArrayList<>();
        }

        if(endHeight - startHeight >= 10000) {
            Log.warn("get block hash more the 10000");
            return new ArrayList<>();
        }

        GetBlocksHashMessage message = new GetBlocksHashMessage(startHeight, endHeight);
        NulsDigestData requestHash = null;
        try {
            requestHash = NulsDigestData.calcDigestData(message.getMsgBody().serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Future<BlockHashResponse> future = DownloadCacheHandler.addGetBlockHashesRequest(requestHash);
        Result hashesResult = messageBusService.sendToNode(message, node, false);
        if (!hashesResult.isSuccess()) {
            DownloadCacheHandler.removeHashesFuture(requestHash);
            return new ArrayList<>();
        }

        long size = (endHeight - startHeight + 1);

        BlockHashResponse response = null;
        try {
            response = future.get(20L, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error(node.getId() + ",start:" + startHeight + " , size:" + size);
            Log.error(e.getMessage());
        } finally {
            DownloadCacheHandler.removeHashesFuture(requestHash);
        }

        if (null == response || response.getHashList() == null || response.getHashList().size() != size) {
            Log.warn("get blocks hashList({}-{}) failed:" + node.getId(), startHeight, size);
            return new ArrayList<>();
        }
        return response.getHashList();
    }

    public static TxGroup getTxGroup(List<NulsDigestData> txHashList, Node node) throws Exception {
        GetTxGroupRequest request = new GetTxGroupRequest();
        GetTxGroupParam param = new GetTxGroupParam();
        param.setTxHashList(txHashList);
        request.setMsgBody(param);
        NulsDigestData requestHash = NulsDigestData.calcDigestData(request.getMsgBody().serialize());
        Future<TxGroup> future = DownloadCacheHandler.addGetTxGroupRequest(requestHash);
        Result result = messageBusService.sendToNode(request, node, false);
        if (result.isFailed()) {
            DownloadCacheHandler.removeTxGroupFuture(requestHash);
            return null;
        }
        try {
            TxGroup txGroup = future.get(30L, TimeUnit.SECONDS);
            return txGroup;
        } catch (Exception e) {
            Log.error(node.getId() + ",get txgroup failed!");
            Log.error(e.getMessage());
            DownloadCacheHandler.removeTxGroupFuture(request.getHash());
            throw e;
        }
    }
}
