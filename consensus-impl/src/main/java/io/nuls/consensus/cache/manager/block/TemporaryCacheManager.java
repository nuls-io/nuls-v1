/*
 *
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
package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.List;

/**
 * Used for sharing temporary data between multiple hander.
 *
 * @author Niels
 * @date 2017/12/12
 */
public class TemporaryCacheManager {
    private static final TemporaryCacheManager INSTANCE = new TemporaryCacheManager();

    private CacheMap<String, BlockHeader> headerCacheMap;
    private CacheMap<String, SmallBlock> smallBlockCacheMap;

    private TemporaryCacheManager() {
    }

    public static TemporaryCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        headerCacheMap = new CacheMap<>("temp" + ConsensusCacheConstant.BLOCK_HEADER_CACHE_NAME, 64, ConsensusCacheConstant.LIVE_TIME, 0);
        smallBlockCacheMap = new CacheMap<>("temp" + ConsensusCacheConstant.SMALL_BLOCK_CACHE_NAME, 32, ConsensusCacheConstant.LIVE_TIME, 0);
    }

    public void cacheSmallBlock(SmallBlock smallBlock) {
        smallBlockCacheMap.put(smallBlock.getBlockHash().getDigestHex(), smallBlock);
    }

    public SmallBlock getSmallBlock(String hash) {
        if (null == smallBlockCacheMap) {
            return null;
        }
        return smallBlockCacheMap.get(hash);
    }

    public void removeSmallBlock(String hash) {
        this.smallBlockCacheMap.remove(hash);
    }

    public void cacheBlockHeader(BlockHeader header) {
        headerCacheMap.put(header.getHash().getDigestHex(), header);
    }

    public BlockHeader getBlockHeader(String hash) {
        if (null == headerCacheMap) {
            return null;
        }
        return headerCacheMap.get(hash);
    }

    public void remove(String hash) {
        if (null == headerCacheMap) {
            return;
        }
        headerCacheMap.remove(hash);
    }


    public void clear() {
        this.smallBlockCacheMap.clear();
        this.headerCacheMap.clear();
    }

    public void destroy() {
        this.smallBlockCacheMap.destroy();
        this.headerCacheMap.destroy();
    }

}
