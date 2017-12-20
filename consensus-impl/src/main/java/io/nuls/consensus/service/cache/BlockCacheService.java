package io.nuls.consensus.service.cache;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockCacheService {
    private static final String BLOCK_CACHE = "blocks";
    private static final String HEIGHT_HASH_CACHE = "blocks-height-hash";
    private static final BlockCacheService INSTANCE = new BlockCacheService();
    private CacheMap<Long, Block> blockCacheMap;
    private CacheMap<String, Long> hashHeightMap;
    private long minHeight;
    private long maxHeight;

    private BlockCacheService() {
        blockCacheMap = new CacheMap<>(BLOCK_CACHE);
        hashHeightMap = new CacheMap<>(HEIGHT_HASH_CACHE);
    }

    public static BlockCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheBlock(Block block) {
        if (block.getHeader().getHeight() == (1 + maxHeight)) {
            maxHeight = block.getHeader().getHeight();
        } else {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        if (block.getHeader().getHeight() < minHeight || minHeight == 0) {
            minHeight = block.getHeader().getHeight();
        }
        blockCacheMap.put(block.getHeader().getHeight(), block);
        hashHeightMap.putWithOutClone(block.getHeader().getHash().getDigestHex(), block.getHeader().getHeight());
    }

    public void clear() {
        this.blockCacheMap.clear();
        this.hashHeightMap.clear();
    }

    public void destroy() {
        this.blockCacheMap.destroy();
        this.hashHeightMap.destroy();
    }

    public long getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(long minHeight) {
        this.minHeight = minHeight;
    }

    public long getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(long maxHeight) {
        this.maxHeight = maxHeight;
    }


    public Block getBlock(long height) {
        return blockCacheMap.get(height);
    }

    public Block getBlock(String hash) {
        long height = hashHeightMap.get(hash);
        return getBlock(height);
    }

    public void removeBlock(long height) {
        Block block = blockCacheMap.get(height);
        blockCacheMap.remove(minHeight);
        hashHeightMap.remove(block.getHeader().getHash().getDigestHex());
        minHeight++;
    }

    public Block getMinHeighBlock() {
        return getBlock(getMinHeight());
    }
}
