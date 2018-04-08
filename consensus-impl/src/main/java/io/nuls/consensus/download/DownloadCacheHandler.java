package io.nuls.consensus.download;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadCacheHandler {

    private static Map<String, CompletableFuture<Block>> blockCacher = new HashMap<>();

    public static CompletableFuture<Block> addGetBlockRequest(String blockHash) {

        CompletableFuture<Block> future = new CompletableFuture<Block>();

        blockCacher.put(blockHash, future);

        return future;
    }

    public static void receiveBlock(Block block) {
        CompletableFuture<Block> future = blockCacher.get(block.getHeader().getHash().getDigestHex());
        if(future != null) {
            future.complete(block);
        }
    }
}
