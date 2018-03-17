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
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.GetBlockHeaderParam;
import io.nuls.consensus.entity.block.BifurcateProcessor;
import io.nuls.consensus.event.GetBlockHeaderEvent;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockCacheManager {
    private static final BlockCacheManager INSTANCE = new BlockCacheManager();

    private EventBroadcaster eventBroadcaster;
    private LedgerService ledgerService;

    private CacheMap<String, BlockHeader> headerCacheMap;
    private CacheMap<String, Block> blockCacheMap;
    private CacheMap<String, SmallBlock> smallBlockCacheMap;

    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();
    private BifurcateProcessor bifurcateProcessor = BifurcateProcessor.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();

    private long storedHeight;
    private long recievedMaxHeight;

    private BlockCacheManager() {
    }

    public static BlockCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
        ledgerService = NulsContext.getServiceBean(LedgerService.class);
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
                if (header.getHeight() > this.recievedMaxHeight) {
                    this.recievedMaxHeight = header.getHeight();
                }
                askNextHeader(nextHeight, sender);

                discard = true;
                break;
            }
            if (height <= bifurcateProcessor.getBestHeight()) {
                discard = false;
                break;

            }
            discard = false;
        } while (false);
        if (discard) {
            return;
        }
        bifurcateProcessor.addHeader(header);
        headerCacheMap.put(header.getHash().getDigestHex(), header);
        if (null != sender) {
            downloadDataUtils.requestSmallBlock(header.getHash(), sender);
            checkNextBlockHeader(header.getHash().getDigestHex(), sender);
        }
    }

    private void checkNextBlockHeader(String preHash, String nodeId) {
        for (BlockHeader header : headerCacheMap.values()) {
            if (header.getPreHash().getDigestHex().equals(preHash)) {
                this.cacheBlockHeader(header, nodeId);
            }
        }
    }

    public BlockHeader getBlockHeader(String hash) {
        if (headerCacheMap == null) {
            return null;
        }
        return headerCacheMap.get(hash);
    }

    public void cacheBlock(Block block) {

        boolean b = this.bifurcateProcessor.addHeader(block.getHeader());
        if (b) {
            NulsContext.getInstance().setBestBlock(block);
        }
        //txs approval
        List<String> blockHashList = bifurcateProcessor.getHashList(block.getHeader().getHeight());
        if (blockHashList.size() > 1) {
            rollbackBlocksTxs(blockHashList);
            return;
        }
        for (int i = 0; i < block.getHeader().getTxCount(); i++) {
            Transaction tx = block.getTxs().get(i);
            tx.setBlockHeight(block.getHeader().getHeight());
            tx.setIndex(i);
            tx.setIndex(i);
            if (tx.getStatus() == null || tx.getStatus() == TxStatusEnum.CACHED) {
                try {
                    this.ledgerService.approvalTx(tx);
                    confirmingTxCacheManager.putTx(tx);
                } catch (NulsException e) {
                    Log.error(e);
                }
            }
        }
        txCacheManager.removeTx(block.getTxHashList());
        blockCacheMap.put(block.getHeader().getHash().getDigestHex(), block);
        Block block1 = this.getBlock(block.getHeader().getHeight());
        if (null != block1 && block1.getHeader().getHeight() > NulsContext.getInstance().getBestBlock().getHeader().getHeight()) {
            NulsContext.getInstance().setBestBlock(block1);
        }
    }

    private void rollbackBlocksTxs(List<String> blockHashList) {
        for (String hash : blockHashList) {
            Block block = getBlock(hash);
            if (null != block) {
                rollbackTxs(block.getTxs());
            }
        }

    }

    private void rollbackTxs(List<Transaction> txs) {
        for (Transaction tx : txs) {
            boolean isMine;
//            try {
//                isMine = ledgerService.checkTxIsMine(tx);
//            } catch (NulsException e) {
//                Log.error(e);
//                throw new NulsRuntimeException(e);
//            }
            if (tx.getStatus() == TxStatusEnum.AGREED
//                    && !isMine
                    ) {
                try {
                    ledgerService.rollbackTx(tx);
                } catch (NulsException e) {
                    Log.error(e);
                }
            }
        }
    }

    public Block getBlock(String hash) {
        if (null == blockCacheMap) {
            return null;
        }
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
        String hash = header.getHash().getDigestHex();
        this.bifurcateProcessor.removeHash(hash);
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

//    private HeaderDigest getNextHeaderDigest1(long height) {
//        HeaderDigest headerDigest = nextHeaderDigest();
//        if (null == headerDigest) {
//            return null;
//        }
//        while (height > headerDigest.getHeight()) {
//            this.bifurcateProcessor.removeHeight(headerDigest.getHeight());
//            headerDigest = nextHeaderDigest();
//            if (null == headerDigest) {
//                return null;
//            }
//        }
//        return headerDigest;
//    }
//
//    private HeaderDigest nextHeaderDigest() {
//        BlockHeaderChain chain = this.bifurcateProcessor.getLongestChain();
//        if (null == chain) {
//            return null;
//        }
//        return chain.getFirst();
//    }

    public Block getBlock(long height) {
        String hash = getDigestHex(height);
        if (hash == null) {
            return null;
        }
        return blockCacheMap.get(hash);
    }

    public BlockHeader getBlockHeader(long height) {
        String hash = getDigestHex(height);
        if (hash == null) {
            return null;
        }
        return headerCacheMap.get(hash);
    }

    public String getDigestHex(long height) {
        List<String> hashList = bifurcateProcessor.getHashList(height);
        if (null == hashList || hashList.isEmpty() || hashList.size() > 1) {
            return null;
        }
        return (hashList.get(0));
    }

    public boolean canPersistence() {
        return null != bifurcateProcessor.getLongestChain() && bifurcateProcessor.getLongestChain().size() > PocConsensusConstant.CONFIRM_BLOCK_COUNT;
    }

    public long getRecievedMaxHeight() {
        return recievedMaxHeight;
    }

    public void askNextHeader(long nextHeight, String nodeId) {
        if (null == nodeId) {
            return;
        }
        GetBlockHeaderEvent event = new GetBlockHeaderEvent();
        event.setEventBody(new GetBlockHeaderParam(nextHeight));
        eventBroadcaster.sendToNode(event, nodeId);
    }

    public void removeBlock(String hash) {
        //todo
    }
}
