package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.core.chain.entity.BlockHeader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockHeaderCacheManager {
    private static final String CACHE_NAME = "block-header-cache";
    private static final BlockHeaderCacheManager INSTANCE = new BlockHeaderCacheManager();

    private BlockHeaderCacheManager() {
    }

    public static BlockHeaderCacheManager getInstance() {
        return INSTANCE;
    }

    private CacheMap<String, BlockHeader> headerCacheMap;
    private Map<Long, Set<String>> hashHeightMap;

    public void init() {
        headerCacheMap = new CacheMap<>(CACHE_NAME, 300000, 0);
        hashHeightMap = new HashMap<>();
    }

    public void clear() {
        headerCacheMap.clear();
    }

    public void cacheHeader(BlockHeader header) {
        headerCacheMap.put(header.getHash().getDigestHex(), header);
        Set<String> hashSet = hashHeightMap.get(header.getHeight());
        if (null == hashSet) {
            hashSet = new HashSet<>();
        }
        hashSet.add(header.getHash().getDigestHex());
        hashHeightMap.put(header.getHeight(), hashSet);
    }

    public Set<String> getBlockHash(long height) {
        return hashHeightMap.get(height);
    }

    public BlockHeader getHeader(String hash) {
        return headerCacheMap.get(hash);
    }

    public void destroy() {
        headerCacheMap.destroy();
    }
}
