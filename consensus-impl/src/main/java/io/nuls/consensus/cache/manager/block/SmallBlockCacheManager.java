package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.SmallBlock;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class SmallBlockCacheManager {
    private static final String CACHE_NAME = "small-block-cache";
    private static final SmallBlockCacheManager INSTANCE = new SmallBlockCacheManager();

    private SmallBlockCacheManager() {
    }

    public static SmallBlockCacheManager getInstance() {
        return INSTANCE;
    }

    private CacheMap<String, SmallBlock> smallBlockCacheMap;

    public void init() {
        smallBlockCacheMap = new CacheMap<>(CACHE_NAME, 300000, 0);
    }

    public void clear() {
        smallBlockCacheMap.clear();
    }

    public void cacheSmallBlock(SmallBlock smallBlock) {
        smallBlockCacheMap.put(smallBlock.getBlockHash().getDigestHex(), smallBlock);
    }

    public SmallBlock getSmallBlock(String hash) {
        return smallBlockCacheMap.get(hash);
    }

    public void destroy() {
        smallBlockCacheMap.destroy();
    }
}
