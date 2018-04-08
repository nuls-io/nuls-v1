package io.nuls.consensus.download;

import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.core.chain.entity.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadCacheHandler {

    private static Map<String, CompletableFuture<Block>> blockCacher = new HashMap<>();
    private static Map<String, CompletableFuture<BlockHashResponse>> blockHashesCacher = new HashMap<>();

    public static CompletableFuture<Block> addGetBlockRequest(String blockHash) {

        CompletableFuture<Block> future = new CompletableFuture<>();

        blockCacher.put(blockHash, future);

        return future;
    }

    public static void receiveBlock(Block block) {
        CompletableFuture<Block> future = blockCacher.get(block.getHeader().getHash().getDigestHex());
        if(future != null) {
            future.complete(block);
        }
    }

    public static CompletableFuture<BlockHashResponse> addGetBlockHashesRequest(String requestHash) {

        CompletableFuture<BlockHashResponse> future = new CompletableFuture<>();

        blockHashesCacher.put(requestHash, future);

        return future;
    }

    public static void receiveHashes(BlockHashResponse hashes) {
        CompletableFuture<BlockHashResponse> future = blockHashesCacher.get(hashes.getRequestEventHash().getDigestHex());
        if(future != null) {
            future.complete(hashes);
        }
    }

    public static void removeGetBlockHashesRequest(String requestHash){
        blockHashesCacher.remove(requestHash);
    }
    public static void removeGetBlockRequest(String requestHash){
        blockCacher.remove(requestHash);
    }
}
