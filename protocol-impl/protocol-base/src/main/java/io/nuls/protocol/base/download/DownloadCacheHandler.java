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

import io.nuls.protocol.constant.NotFoundType;
import io.nuls.protocol.event.entity.BlockHashResponse;
import io.nuls.protocol.event.entity.NotFound;
import io.nuls.protocol.model.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadCacheHandler {

    //TODO 定期清除缓存，否则内存有可能被撑爆

    private static Map<String, CompletableFuture<Block>> blockCacher = new HashMap<>();
    private static Map<String, CompletableFuture<BlockHashResponse>> blockHashesCacher = new HashMap<>();

    public static CompletableFuture<Block> addGetBlockRequest(String blockHash) {

        CompletableFuture<Block> future = new CompletableFuture<>();

        blockCacher.put(blockHash, future);

        return future;
    }

    public static void receiveBlock(Block block) {
        String hash = block.getHeader().getHash().getDigestHex();
        CompletableFuture<Block> future = blockCacher.get(hash);
        if (future != null) {
            future.complete(block);
            blockCacher.remove(hash);
        }
    }

    public static CompletableFuture<BlockHashResponse> addGetBlockHashesRequest(String requestHash) {

        CompletableFuture<BlockHashResponse> future = new CompletableFuture<>();

        blockHashesCacher.put(requestHash, future);

        return future;
    }

    public static void receiveHashes(BlockHashResponse hashes) {
        String key = hashes.getRequestEventHash().getDigestHex();
        CompletableFuture<BlockHashResponse> future = blockHashesCacher.get(key);
        if (future != null) {
            future.complete(hashes);
            blockHashesCacher.remove(key);
        }
    }

    public static void notFoundBlock(NotFound data) {
        String hash = data.getHash().getDigestHex();
        if (data.getType()== NotFoundType.BLOCK) {
            CompletableFuture<Block> future = blockCacher.get(hash);
            if (future != null) {
                future.complete(null);
                blockCacher.remove(hash);
            }
        }else if(data.getType()==NotFoundType.TRANSACTION){
            //todo
        }else if(data.getType()==NotFoundType.HASHES){
            CompletableFuture<BlockHashResponse> future = blockHashesCacher.get(hash);
            if (future != null) {
                future.complete(null);
                blockHashesCacher.remove(hash);
            }
        }
    }
}
