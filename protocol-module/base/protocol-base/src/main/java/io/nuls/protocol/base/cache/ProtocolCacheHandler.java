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

package io.nuls.protocol.base.cache;

import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.protocol.base.utils.filter.InventoryFilter;
import io.nuls.protocol.constant.MessageDataType;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.model.TxGroup;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author ln
 * @date 2018/4/8
 */
public class ProtocolCacheHandler {

    public static final InventoryFilter TX_FILTER = new InventoryFilter();
    public static final InventoryFilter SMALL_BLOCK_FILTER = new InventoryFilter();

    private static DataCacher<Block> blockCacher = new DataCacher<>(MessageDataType.BLOCK);
    private static DataCacher<TxGroup> txGroupCacher = new DataCacher<>(MessageDataType.TRANSACTIONS);
    private static DataCacher<BlockHashResponse> blockHashesCacher = new DataCacher<>(MessageDataType.HASHES);
    private static DataCacher<CompleteParam> taskCacher = new DataCacher<>(MessageDataType.BLOCKS);
    private static DataCacher<NulsDigestData> reactCacher = new DataCacher<>(MessageDataType.REQUEST);
    private static DataCacher<Boolean> txCacher = new DataCacher<>(MessageDataType.TRANSACTION);
    private static DataCacher<Boolean> smallBlockCacher = new DataCacher<>(MessageDataType.SMALL_BLOCK);

    public static CompletableFuture<Boolean> addGetTxRequest(NulsDigestData txHash) {
        return txCacher.addFuture(txHash);
    }

    public static void receiveTx(NulsDigestData txHash, boolean log) {
        TX_FILTER.insert(txHash.getDigestBytes());
        txCacher.callback(txHash, true, log);

    }

    public static void removeTxFuture(NulsDigestData txHash) {
        txCacher.removeFuture(txHash);
    }

    public static CompletableFuture<Boolean> addGetSmallBlockRequest(NulsDigestData blockHash) {
        return smallBlockCacher.addFuture(blockHash);
    }

    public static void receiveSmallBlock(NulsDigestData blockHash, boolean log) {
        SMALL_BLOCK_FILTER.insert(blockHash.getDigestBytes());
        smallBlockCacher.callback(blockHash, true, log);
    }

    public static void removeSmallBlockFuture(NulsDigestData blockHash) {
        smallBlockCacher.removeFuture(blockHash);
    }

    public static CompletableFuture<Block> addGetBlockRequest(NulsDigestData requestHash) {
        return blockCacher.addFuture(requestHash);
    }

    public static void receiveBlock(Block block) {
        NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(block.getHeader().getHeight()));
        boolean result = blockCacher.callback(hash, block);
        if (!result) {
            blockCacher.callback(block.getHeader().getHash(), block);
        }
    }

    public static CompletableFuture<BlockHashResponse> addGetBlockHashesRequest(NulsDigestData requestHash) {
        return blockHashesCacher.addFuture(requestHash);
    }

    public static void receiveHashes(BlockHashResponse hashes) {
        blockHashesCacher.callback(hashes.getRequestMessageHash(), hashes);
    }

    public static void receiveTxGroup(TxGroup txGroup) {
        txGroupCacher.callback(txGroup.getRequestHash(), txGroup);
    }

    public static Future<TxGroup> addGetTxGroupRequest(NulsDigestData hash) {
        return txGroupCacher.addFuture(hash);
    }

    public static Future<CompleteParam> addTaskRequest(NulsDigestData hash) {
        return taskCacher.addFuture(hash);
    }

    public static void notFound(NotFound data) {
        if (data.getType() == MessageDataType.BLOCK) {
            blockCacher.notFound(data.getHash());
        } else if (data.getType() == MessageDataType.BLOCKS) {
            taskCacher.notFound(data.getHash());
        } else if (data.getType() == MessageDataType.TRANSACTIONS) {
            txGroupCacher.notFound(data.getHash());
        } else if (data.getType() == MessageDataType.HASHES) {
            blockHashesCacher.notFound(data.getHash());
        }
    }

    public static void taskComplete(CompleteParam param) {
        taskCacher.callback(param.getRequestHash(), param);
    }

    public static Future<NulsDigestData> addRequest(NulsDigestData requesetId) {
        return reactCacher.addFuture(requesetId);

    }

    public static void requestReact(NulsDigestData requesetId) {
        reactCacher.callback(requesetId, requesetId);
    }

    public static void removeBlockFuture(NulsDigestData hash) {
        blockCacher.removeFuture(hash);
    }

    public static void removeHashesFuture(NulsDigestData hash) {
        blockHashesCacher.removeFuture(hash);
    }

    public static void removeTxGroupFuture(NulsDigestData hash) {
        txGroupCacher.removeFuture(hash);
    }

    public static void removeTaskFuture(NulsDigestData hash) {
        taskCacher.removeFuture(hash);
    }

    public static void removeRequest(NulsDigestData requesetId) {
        reactCacher.removeFuture(requesetId);
    }

}
