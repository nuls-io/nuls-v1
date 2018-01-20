/**
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
 */
package io.nuls.consensus.service.impl;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockServiceImpl implements io.nuls.consensus.service.intf.BlockService {

    private BlockStorageService blockStorageService = BlockStorageService.getInstance();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public Block getGengsisBlock() {
        try {
            return blockStorageService.getBlock(0);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public long getLocalHeight() {
        long height = blockCacheManager.getBestHeight();
        if (height == 0) {
            height = blockStorageService.getBestHeight();
        }
        return height;
    }

    @Override
    public Block getLocalBestBlock() {
        return getBlock(getLocalHeight());
    }

    @Override
    public BlockHeader getBlockHeader(long height) {
        BlockHeader header;
        if (height <= blockCacheManager.getStoredHeight()) {
            header = blockStorageService.getBlockHeader(height);
        } else {
            header = blockCacheManager.getBlockHeader(height);
        }
        return header;
    }

    @Override
    public Block getBlock(String hash) {
        Block block = blockCacheManager.getBlock(hash);
        if (null == block) {
            try {
                block = blockStorageService.getBlock(hash);
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return block;
    }

    @Override
    public Block getBlock(long height) {
        Block block = blockCacheManager.getBlock(height);
        if (null == block) {
            try {
                block = blockStorageService.getBlock(height);
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return block;
    }

    @Override
    public List<Block> getBlockList(long startHeight, long endHeight) {
        return blockStorageService.getBlockList(startHeight, endHeight);
    }


    @Override
    @TransactionalAnnotation
    public void saveBlock(Block block) throws IOException {
        for (int x = 0; x < block.getHeader().getTxCount(); x++) {
            Transaction tx = block.getTxs().get(x);
            tx.setBlockHeight(block.getHeader().getHeight());
            try {
                ledgerService.commitTx(tx);
            } catch (Exception e) {
                Log.error(e);
                rollback(block.getTxs(), x);
                throw new NulsRuntimeException(e);
            }
        }
        blockStorageService.save(block.getHeader());
        ledgerService.saveTxList(block.getTxs());
    }


    @Override
    @TransactionalAnnotation
    public void rollbackBlock(long height) {
        Block block = this.getBlock(height);
        if (null == block) {
            return;
        }
        this.rollback(block.getTxs(), block.getTxs().size() - 1);
        blockStorageService.delete(block.getHeader().getHash().getDigestHex());
    }

    @Override
    public List<NulsDigestData> getBlockHashList(long startHeight, long endHeight, long split) {
        return blockStorageService.getBlockHashList(startHeight, endHeight, split);
    }

    @Override
    public BlockHeader getBlockHeader(NulsDigestData hash) {
        String hashHex = hash.getDigestHex();
        BlockHeader header = blockCacheManager.getBlockHeader(hashHex);
        if (null == header) {
            header = blockStorageService.getBlockHeader(hashHex);
        }
        return header;
    }

    private void rollback(List<Transaction> txs, int max) {
        for (int x = 0; x < max; x++) {
            Transaction tx = txs.get(x);
            try {
                ledgerService.rollbackTx(tx);
            } catch (NulsException e) {
                Log.error(e);
            }
        }

    }
}
