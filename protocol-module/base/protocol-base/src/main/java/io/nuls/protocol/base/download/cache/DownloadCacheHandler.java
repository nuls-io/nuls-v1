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

package io.nuls.protocol.base.download.cache;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.protocol.constant.NotFoundType;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.NotFound;
import io.nuls.protocol.model.TxGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadCacheHandler {

    private static Map<NulsDigestData, CompletableFuture<Block>> blockCacher = new HashMap<>();
    private static Map<NulsDigestData, CompletableFuture<TxGroup>> txGroupCacher = new HashMap<>();
    private static Map<NulsDigestData, CompletableFuture<BlockHashResponse>> blockHashesCacher = new HashMap<>();
    private static Map<NulsDigestData, CompletableFuture<CompleteParam>> taskCacher = new HashMap<>();

    public static CompletableFuture<Block> addGetBlockRequest(NulsDigestData blockHash) {

        CompletableFuture<Block> future = new CompletableFuture<>();

        blockCacher.put(blockHash, future);

        return future;
    }

    public static void receiveBlock(Block block) {
        NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(block.getHeader().getHeight()));
        CompletableFuture<Block> future = blockCacher.get(hash);

        if(future == null) {
            hash = block.getHeader().getHash();
            future = blockCacher.get(hash);
        }
        if (future != null) {
            future.complete(block);
            blockCacher.remove(hash);
        }  else {
            Log.warn("Block null : " + block.getHeader().getHash());
        }
    }

    public static CompletableFuture<BlockHashResponse> addGetBlockHashesRequest(NulsDigestData requestHash) {

        CompletableFuture<BlockHashResponse> future = new CompletableFuture<>();

        blockHashesCacher.put(requestHash, future);

        return future;
    }

    public static void receiveHashes(BlockHashResponse hashes) {
        CompletableFuture<BlockHashResponse> future = blockHashesCacher.get(hashes.getRequestMessageHash());
        if (future != null) {
            future.complete(hashes);
            blockHashesCacher.remove(hashes.getRequestMessageHash());
        } else {
            Log.warn("hashes null : " + hashes.getRequestMessageHash());
        }
    }

    public static void receiveTxGroup(TxGroup txGroup) {
        CompletableFuture<TxGroup> future = txGroupCacher.get(txGroup.getRequestHash());
        if (null != future) {
            future.complete(txGroup);
            txGroupCacher.remove(txGroup.getRequestHash());
        } else {
            Log.warn("txGroup null : " + txGroup.getRequestHash());
        }
    }

    public static Future<TxGroup> addGetTxGroupRequest(NulsDigestData hash) {
        CompletableFuture<TxGroup> future = new CompletableFuture<>();
        txGroupCacher.put(hash, future);
        return future;
    }
    public static Future<CompleteParam> addTaskRequest(NulsDigestData hash) {
        CompletableFuture<CompleteParam> future = new CompletableFuture<>();
        taskCacher.put(hash, future);
        return future;
    }

    public static void notFoundBlock(NotFound data) {
        if (data.getType() == NotFoundType.BLOCK) {
            CompletableFuture<Block> future = blockCacher.get(data.getHash());
            if (future != null) {
                future.complete(null);
                blockCacher.remove(data.getHash());
            } else {
                Log.warn("BLOCK NotFound null : " + data.getHash());
            }
        } else if (data.getType() == NotFoundType.BLOCKS) {
            CompletableFuture<CompleteParam> future = taskCacher.get(data.getHash());
            if (future != null) {
                future.complete(new CompleteParam(data.getHash(), false));
                taskCacher.remove(data.getHash());
            } else {
                Log.warn("BLOCKS NotFound null : " + data.getHash());
            }
        } else if (data.getType() == NotFoundType.TRANSACTION) {
            CompletableFuture<TxGroup> future = txGroupCacher.get(data.getHash());
            if (future != null) {
                future.complete(null);
                txGroupCacher.remove(data.getHash());
            } else {
                Log.warn("TRANSACTION NotFound null : " + data.getHash());
            }
        } else if (data.getType() == NotFoundType.HASHES) {
            CompletableFuture<BlockHashResponse> future = blockHashesCacher.get(data.getHash());
            if (future != null) {
                future.complete(null);
                blockHashesCacher.remove(data.getHash());
            } else {
                Log.warn("HASHES NotFound null : " + data.getHash());
            }
        }
    }
    public static void taskComplete(CompleteParam param) {
        CompletableFuture<CompleteParam> future = taskCacher.get(param.getRequestHash());
        if (future != null) {
            future.complete(param);
            taskCacher.remove(param.getRequestHash());
        } else {
            Log.warn("BLOCKS NotFound null : " + param.getRequestHash());
        }
    }

    public static void removeBlockFuture(NulsDigestData hash) {
        blockCacher.remove(hash);
    }

    public static void removeHashesFuture(NulsDigestData hash) {
        blockHashesCacher.remove(hash);
    }

    public static void removeTxGroupFuture(NulsDigestData hash) {
        txGroupCacher.remove(hash);
    }

    public static void removeTaskFuture(NulsDigestData hash) {
        taskCacher.remove(hash);
    }
}
