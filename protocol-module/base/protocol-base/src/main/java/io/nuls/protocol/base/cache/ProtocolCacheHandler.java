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
import io.nuls.protocol.constant.MessageDataType;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.NotFound;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author ln
 */
public class ProtocolCacheHandler {


    private static DataCacher<Block> blockByHashCacher = new DataCacher<>(MessageDataType.BLOCK);
    private static DataCacher<Block> blockByHeightCacher = new DataCacher<>(MessageDataType.BLOCK);
//    private static DataCacher<TxGroup> txGroupCacher = new DataCacher<>(MessageDataType.TRANSACTIONS);
    private static DataCacher<BlockHashResponse> blockHashesCacher = new DataCacher<>(MessageDataType.HASHES);
    private static DataCacher<CompleteParam> taskCacher = new DataCacher<>(MessageDataType.BLOCKS);
    private static DataCacher<NulsDigestData> reactCacher = new DataCacher<>(MessageDataType.REQUEST);
//    private static DataCacher<Transaction> txCacher = new DataCacher<>(MessageDataType.TRANSACTION);
//    private static DataCacher<SmallBlock> smallBlockCacher = new DataCacher<>(MessageDataType.SMALL_BLOCK);

//    public static CompletableFuture<Transaction> addGetTxRequest(NulsDigestData txHash) {
//        return txCacher.addFuture(txHash);
//    }

    public static CompletableFuture<Block> addGetBlockByHeightRequest(NulsDigestData requestHash) {
        return blockByHeightCacher.addFuture(requestHash);
    }

    public static CompletableFuture<Block> addGetBlockByHashRequest(NulsDigestData requestHash) {
        return blockByHashCacher.addFuture(requestHash);
    }

    public static void receiveBlock(Block block) {
        NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(block.getHeader().getHeight()));
        boolean result = blockByHeightCacher.callback(hash, block, false);
        if (!result) {
            blockByHashCacher.callback(block.getHeader().getHash(), block);
        }
    }

    public static CompletableFuture<BlockHashResponse> addGetBlockHashesRequest(NulsDigestData requestHash) {
        return blockHashesCacher.addFuture(requestHash);
    }

    public static void receiveHashes(BlockHashResponse hashes) {
        blockHashesCacher.callback(hashes.getRequestMessageHash(), hashes);
    }

    public static Future<CompleteParam> addTaskRequest(NulsDigestData hash) {
        return taskCacher.addFuture(hash);
    }

    public static void notFound(NotFound data) {
        if (data.getType() == MessageDataType.BLOCK) {
            blockByHeightCacher.notFound(data.getHash());
            blockByHashCacher.notFound(data.getHash());
        } else if (data.getType() == MessageDataType.BLOCKS) {
            taskCacher.notFound(data.getHash());
        } else if (data.getType() == MessageDataType.HASHES) {
            blockHashesCacher.notFound(data.getHash());
        }
//        else if (data.getType() == MessageDataType.TRANSACTIONS) {
//            txGroupCacher.notFound(data.getHash());
//        } else if (data.getType() == MessageDataType.TRANSACTION) {
//            txCacher.notFound(data.getHash());
//        } else if (data.getType() == MessageDataType.SMALL_BLOCK) {
//            smallBlockCacher.notFound(data.getHash());
//        }
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

    public static void removeBlockByHeightFuture(NulsDigestData hash) {
        blockByHeightCacher.removeFuture(hash);
    }

    public static void removeBlockByHashFuture(NulsDigestData hash) {
        blockByHashCacher.removeFuture(hash);
    }

    public static void removeHashesFuture(NulsDigestData hash) {
        blockHashesCacher.removeFuture(hash);
    }

    public static void removeTaskFuture(NulsDigestData hash) {
        taskCacher.removeFuture(hash);
    }

    public static void removeRequest(NulsDigestData requesetId) {
        reactCacher.removeFuture(requesetId);
    }

}
