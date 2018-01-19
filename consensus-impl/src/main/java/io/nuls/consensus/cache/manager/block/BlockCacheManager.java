/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.block.BifurcateProcessor;
import io.nuls.consensus.entity.block.BlockHeaderChain;
import io.nuls.consensus.entity.block.HeaderDigest;
import io.nuls.consensus.event.GetBlockHeaderEvent;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockCacheManager {
    private static final String HEIGHT_HASH_CACHE = "blocks-height-hash";
    private static final BlockCacheManager INSTANCE = new BlockCacheManager();

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    private CacheMap<String, BlockHeader> headerCacheMap;
    private CacheMap<String, Block> blockCacheMap;
    private CacheMap<String, SmallBlock> smallBlockCacheMap;

    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();
    private BifurcateProcessor bifurcateProcessor = BifurcateProcessor.getInstance();

    private long storedHeight;

    private BlockCacheManager() {
    }

    public static BlockCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        smallBlockCacheMap = new CacheMap<>(ConsensusCacheConstant.SMALL_BLOCK_CACHE_NAME, 32, ConsensusCacheConstant.LIVE_TIME, 0);
        blockCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_CACHE_NAME, 64, ConsensusCacheConstant.LIVE_TIME, 0);
        headerCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_HEADER_CACHE_NAME, 32, ConsensusCacheConstant.LIVE_TIME, 0);
    }

    public void cacheBlockHeader(BlockHeader header, String sender) {
        long height = header.getHeight();
        boolean discard = true;
        do {
            ValidateResult result = header.verify();
            if (result.isFailed()) {
                discard = true;
                break;
            }
            if (height <= storedHeight) {
                discard = true;
                break;
            }
            long nextHeight = 1 + bifurcateProcessor.getBestHeight();
            if (height > nextHeight) {
                headerCacheMap.put(header.getHash().getDigestHex(), header);
                GetBlockHeaderEvent event = new GetBlockHeaderEvent();
                event.setEventBody(new BasicTypeData<>(height - 1));
                eventBroadcaster.sendToNode(event, sender);
                discard = true;
                break;
            }
            if (height <= bifurcateProcessor.getBestHeight()) {
                discard = false;
                break;

            }
        } while (false);
        if (discard) {
            return;
        }
        bifurcateProcessor.addHeader(header);
        headerCacheMap.put(header.getHash().getDigestHex(), header);
        downloadDataUtils.requestSmallBlock(header.getHash(), sender);
        checkNextBlockHeader(header.getHash().getDigestHex(), sender);
    }

    private void checkNextBlockHeader(String preHash, String nodeId) {
        for (BlockHeader header : headerCacheMap.values()) {
            if (header.getPreHash().getDigestHex().equals(preHash)) {
                this.cacheBlockHeader(header, nodeId);
            }
        }
    }

    public BlockHeader getBlockHeader(String hash) {
        return headerCacheMap.get(hash);
    }

    public void cacheBlock(Block block) {
        blockCacheMap.put(block.getHeader().getHash().getDigestHex(), block);
    }

    public Block getBlock(String hash) {
        return blockCacheMap.get(hash);
    }

    public void cacheSmallBlock(SmallBlock smallBlock, String nodeId) {
        smallBlockCacheMap.put(smallBlock.getBlockHash().getDigestHex(), smallBlock);
        downloadDataUtils.requestTxGroup(smallBlock.getBlockHash(), nodeId);
    }

    public SmallBlock getSmallBlock(String hash) {
        return smallBlockCacheMap.get(hash);
    }

    public void clear() {
        this.blockCacheMap.clear();
        this.headerCacheMap.clear();
        this.smallBlockCacheMap.clear();
    }

    public void destroy() {
        this.blockCacheMap.destroy();
        this.headerCacheMap.destroy();
        this.smallBlockCacheMap.destroy();
    }

    public void removeBlock(BlockHeader header) {
        if (null == header) {
            return;
        }
        this.bifurcateProcessor.removeHeight(header.getHeight());
        String hash = header.getHash().getDigestHex();
        this.blockCacheMap.remove(hash);
        this.smallBlockCacheMap.remove(hash);
        this.headerCacheMap.remove(hash);
    }

    public long getBestHeight() {
        return this.bifurcateProcessor.getBestHeight();
    }

    public long getStoredHeight() {
        return storedHeight;
    }

    public void setStoredHeight(long storedHeight) {
        this.storedHeight = storedHeight;
    }

    private HeaderDigest getNextHeaderDigest(long height) {
        HeaderDigest headerDigest = nextHeaderDigest();
        if (null == headerDigest) {
            return null;
        }
        while (height > headerDigest.getHeight()) {
            this.bifurcateProcessor.removeHeight(headerDigest.getHeight());
            headerDigest = nextHeaderDigest();
            if (null == headerDigest) {
                return null;
            }
        }
        return headerDigest;
    }

    private HeaderDigest nextHeaderDigest() {
        BlockHeaderChain chain = this.bifurcateProcessor.getLongestChain();
        if (null == chain) {
            return null;
        }
        return chain.getFirst();
    }

    public Block getBlock(long height) {
        HeaderDigest headerDigest = getNextHeaderDigest(height);
        if (null == headerDigest) {
            return null;
        }
        return blockCacheMap.get(headerDigest.getHash());
    }

    public BlockHeader getBlockHeader(long height) {
        HeaderDigest headerDigest = getNextHeaderDigest(height);
        if (null == headerDigest) {
            return null;
        }
        return headerCacheMap.get(headerDigest.getHash());
    }

    public boolean canPersistence() {

        return null != bifurcateProcessor.getLongestChain() && bifurcateProcessor.getLongestChain().size() > PocConsensusConstant.CONFIRM_BLOCK_COUNT;
    }
}
