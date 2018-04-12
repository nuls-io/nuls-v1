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

import io.nuls.account.entity.Address;
import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;

import java.util.List;

/**
 * cache block data
 * @author Niels
 * @date 2017/12/12
 */
public class ConfirmingBlockCacheManager {
    private static final ConfirmingBlockCacheManager INSTANCE = new ConfirmingBlockCacheManager();

    private CacheMap<String, BlockHeader> headerCacheMap;
    private CacheMap<String, List<Transaction>> txsCacheMap;

    private ConfirmingBlockCacheManager() {
    }

    public static ConfirmingBlockCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        txsCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_TXS_CACHE_NAME, 256, 1000000, 1000000);
        headerCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_HEADER_CACHE_NAME, 64, 1000000, 1000000);
    }

    public BlockHeader getBlockHeader(String hash) {
        if (headerCacheMap == null) {
            return null;
        }
        return headerCacheMap.get(hash);
    }

    public boolean cacheBlock(Block block) {
        String hash = block.getHeader().getHash().getDigestHex();
        headerCacheMap.put(hash, block.getHeader());
        txsCacheMap.put(hash, block.getTxs());
        BlockLog.debug("cache block height:" +block.getHeader().getHeight() + ", preHash:" + block.getHeader().getPreHash() + " , hash:" + block.getHeader().getHash() + ", address:" + Address.fromHashs(block.getHeader().getPackingAddress()));
        if(null==headerCacheMap.get(hash)){
            System.out.println();
        }
        return true;
    }


    public Block getBlock(String hash) {
        if (null == txsCacheMap || headerCacheMap == null) {
            return null;
        }
        BlockHeader header = headerCacheMap.get(hash);
        List<Transaction> txs = txsCacheMap.get(hash);
        if (null == header || null == txs || txs.isEmpty()) {
            return null;
        }
        Block block = new Block();
        block.setHeader(header);
        block.setTxs(txs);
        return block;
    }

    public void clear() {
        BlockLog.debug("clear cached block hash！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
        this.txsCacheMap.clear();
        this.headerCacheMap.clear();
    }

    public void destroy() {
        this.txsCacheMap.destroy();
        this.headerCacheMap.destroy();
    }

    public void removeBlock(String hash) {
        BlockLog.debug("remove cached block hash:"+hash);
        this.txsCacheMap.remove(hash);
        this.headerCacheMap.remove(hash);
    }

    public CacheMap<String, BlockHeader> getHeaderCacheMap() {
        return headerCacheMap;
    }

    public CacheMap<String, List<Transaction>> getTxsCacheMap() {
        return txsCacheMap;
    }
}
