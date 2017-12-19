package io.nuls.consensus.service.cache;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockCacheService {
    private static final String BLOCK_CACHE = "blocks";
    private static final String HEIGHT_HASH_CACHE = "blocks";
    private static final BlockCacheService INSTANCE = new BlockCacheService();
    private CacheService cacheService = NulsContext.getInstance().getService(CacheService.class);
    private long minHeight;
    private long nowHeight;

    private BlockCacheService() {
        this.cacheService.createCache(BLOCK_CACHE);
    }

    public static BlockCacheService getInstance() {
        return INSTANCE;
    }


    public void cacheBlock(Block block) {
        //todo 触发持久化检查，缓存block
        cacheService.putElement(BLOCK_CACHE, block.getHeader().getHeight(), block);


    }

    public void clear() {
        // todo auto-generated method stub(niels)

    }

    public long getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(long minHeight) {
        this.minHeight = minHeight;
    }

    public long getNowHeight() {
        return nowHeight;
    }

    public void setNowHeight(long nowHeight) {
        this.nowHeight = nowHeight;
    }
}
