package io.nuls.consensus.service.cache;

import io.nuls.core.chain.entity.BlockHeader;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockHeaderCacheService {
    private static final BlockHeaderCacheService INSTANCE = new BlockHeaderCacheService();
    private BlockHeaderCacheService(){}
    public static BlockHeaderCacheService getInstance(){
        return INSTANCE;
    }

    public void init(){
        // todo auto-generated method stub(niels)
    }

    public void clear() {
        // todo auto-generated method stub(niels)

    }

    public void cacheHeader(BlockHeader header) {
        // todo auto-generated method stub(niels)
    }

    public BlockHeader getHeader(long height) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
