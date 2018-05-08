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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.base.download.cache.DownloadCacheHandler;
import io.nuls.protocol.message.GetBlockRequest;
import io.nuls.protocol.message.GetBlocksHashRequest;
import io.nuls.protocol.message.GetTxGroupRequest;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.GetTxGroupParam;
import io.nuls.protocol.model.TxGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadUtils {

    private static MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    public static Block getBlockByHash(NulsDigestData hash, Node node) {
        List<Block> blocks = null;
        try {
            blocks = getBlocks(node, hash, hash, -1L, 1);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        if (blocks == null || blocks.size() == 0) {
            return null;
        } else {
            return blocks.get(0);
        }
    }

    public static List<Block> getBlocks(Node node, NulsDigestData startHash, NulsDigestData endHash, long startHeight, int size) throws Exception {

        List<Block> resultList = new ArrayList<Block>();

        if (startHash.equals(endHash)) {
            GetBlockRequest request = new GetBlockRequest(startHeight, (long) size,
                    startHash, endHash);
            Future<Block> future = DownloadCacheHandler.addGetBlockRequest(endHash);
            Result result = messageBusService.sendToNode(request, node, false);
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
                Log.error(e.getMessage());
                throw e;
            }
        } else {
            GetBlocksHashRequest hashesRequest = new GetBlocksHashRequest(startHeight, size);
            Future<BlockHashResponse> hashesFuture = DownloadCacheHandler.addGetBlockHashesRequest(hashesRequest.getHash());
            Result hashesResult = messageBusService.sendToNode(hashesRequest, node, false);
            if (!hashesResult.isSuccess()) {
                return resultList;
            }
            BlockHashResponse response;
            try {
                response = hashesFuture.get(20L, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.error(node.getId() + ",start:" + startHeight + " , size:" + size);
                Log.error(e.getMessage());
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
                Future<Block> future = DownloadCacheHandler.addGetBlockRequest(hash);
                futureList.add(future);
            }
            Result result = messageBusService.sendToNode(request, node, false);
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
                    Log.error(e.getMessage());
                    throw e;
                }
            }
        }
        return resultList;
    }


    public static TxGroup getTxGroup(List<NulsDigestData> txHashList, Node node) throws Exception {
        GetTxGroupRequest request = new GetTxGroupRequest();
        GetTxGroupParam param = new GetTxGroupParam();
        param.setTxHashList(txHashList);
        request.setMsgBody(param);
        Future<TxGroup> future = DownloadCacheHandler.addGetTxGroupRequest(request.getHash());
        Result result = messageBusService.sendToNode(request, node, false);
        if (result.isFailed()) {
            return null;
        }
        try {
            TxGroup txGroup = future.get(30L, TimeUnit.SECONDS);
            return txGroup;
        } catch (Exception e) {
            Log.error(node.getId() + ",get txgroup failed!");
            Log.error(e.getMessage());
            throw e;
        }
    }
}
