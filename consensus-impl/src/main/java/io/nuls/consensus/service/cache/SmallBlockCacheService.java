package io.nuls.consensus.service.cache;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.SmallBlock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class SmallBlockCacheService {
    private static final String CACHE_NAME = "small-block-cache";
    private static final SmallBlockCacheService INSTANCE = new SmallBlockCacheService();

    private SmallBlockCacheService() {
    }

    public static SmallBlockCacheService getInstance() {
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
        smallBlockCacheMap.putWithOutClone(smallBlock.getBlockHash().getDigestHex(), smallBlock);
    }

    public SmallBlock getSmallBlock(String hash) {
        return smallBlockCacheMap.get(hash);
    }

    public void destroy() {
        smallBlockCacheMap.destroy();
    }
}
