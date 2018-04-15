/*
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
package io.nuls.protocol.base.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.core.utils.log.ConsensusLog;
import io.nuls.protocol.base.entity.block.BlockRoundData;
import io.nuls.protocol.base.manager.BlockManager;
import io.nuls.protocol.intf.BlockService;
import io.nuls.protocol.utils.BlockHeightComparator;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.BlockLog;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockServiceImpl implements BlockService {
    private BlockStorageService blockStorageService = BlockStorageService.getInstance();
    private BlockManager blockManager = BlockManager.getInstance();
    @Autowired
    private LedgerService ledgerService;

    @Override
    public Block getGengsisBlock() {
        try {
            return blockStorageService.getBlock(0);
        } catch (Exception e) {
            Log.error(e);
            Log.error(e);
        }
        return null;
    }

    @Override
    public long getLocalHeight() {
        long height = NulsContext.getInstance().getBestHeight();
        if (height == 0) {
            height = blockStorageService.getBestHeight();
        }
        return height;
    }

    @Override
    public long getLocalSavedHeight() {
        return blockStorageService.getBestHeight();
    }

    @Override
    public Block getLocalBestBlock() {
        Block block = getBlock(getLocalHeight());
        if (null == block) {
            block = NulsContext.getInstance().getBestBlock();
        }
        return block;
    }

    @Override
    public BlockHeader getBlockHeader(long height) throws NulsException {
        return blockStorageService.getBlockHeader(height);
    }

    @Override
    public BlockHeader getBlockHeader(String hash) throws NulsException {
        return blockStorageService.getBlockHeader(hash);
    }

    @Override
    public Block getBlock(String hash) {
        Block block = null;
        try {
            block = blockStorageService.getBlock(hash);
        } catch (Exception e) {
            Log.error(e);
        }
        return block;
    }

    @Override
    public Block getBlock(long height) {
        try {
            return blockStorageService.getBlock(height);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public List<Block> getBlockList(long startHeight, long endHeight) throws NulsException {
        List<Block> blockList = blockStorageService.getBlockList(startHeight, endHeight);
        if (blockList.size() < (endHeight - startHeight + 1)) {
            long currentMaxHeight = blockList.get(blockList.size() - 1).getHeader().getHeight();
            while (currentMaxHeight < endHeight) {
                long next = currentMaxHeight + 1;
                Block block = blockManager.getBlock(next);
                if (null == block) {
                    try {
                        block = blockStorageService.getBlock(next);
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
                if (null != block) {
                    blockList.add(block);
                }
            }
        }
        Collections.sort(blockList, BlockHeightComparator.getInstance());
        return blockList;
    }


    @Override
    @DbSession
    public boolean saveBlock(Block block) throws IOException {
        ConsensusLog.info("save block height:" + block.getHeader().getHeight() + ", preHash:" + block.getHeader().getPreHash() + " , hash:" + block.getHeader().getHash() + ", address:" + Address.fromHashs(block.getHeader().getPackingAddress()));
        ValidateResult result = block.verify();
        boolean b = false;
        if (result.isFailed() && result.getErrorCode() != ErrorCode.ORPHAN_TX && ErrorCode.ORPHAN_BLOCK != result.getErrorCode()) {
            throw new NulsRuntimeException(result.getErrorCode(), result.getMessage());
        } else if (result.getErrorCode() == ErrorCode.ORPHAN_TX || ErrorCode.ORPHAN_BLOCK == result.getErrorCode()) {
            b = true;
        }
        for (int x = 0; x < block.getHeader().getTxCount(); x++) {
            Transaction tx = block.getTxs().get(x);
            tx.setBlockHeight(block.getHeader().getHeight());
            if (tx.getStatus()==null||tx.getStatus() == TxStatusEnum.CACHED) {
                b = true;
                try {
                    ledgerService.approvalTx(tx, block);
                } catch (Exception e) {
                    Log.error(e);
                    rollback(block.getTxs(), x);
                    throw new NulsRuntimeException(e);
                }
            }
        }
        if (b) {
            block.verifyWithException();
        }
        for (int x = 0; x < block.getHeader().getTxCount(); x++) {
            Transaction tx = block.getTxs().get(x);
            tx.setIndex(x);
            tx.setBlockHeight(block.getHeader().getHeight());
            if (tx.getStatus() == TxStatusEnum.AGREED) {
                try {
                    ledgerService.commitTx(tx, block);
                } catch (Exception e) {
                    Log.error(e);
                    rollback(block.getTxs(), x);
                    throw new NulsRuntimeException(e);
                }
            }
        }
        ledgerService.saveTxList(block.getTxs());
        blockStorageService.save(block);
        return true;
    }


    @Override
    @DbSession
    public void rollbackBlock(String hash) {
        Block block = this.getBlock(hash);
        if (null == block) {
            return;
        }
        boolean result = this.blockManager.rollback(block);
        if (result) {
            return;
        }
        this.rollback(block.getTxs(), block.getTxs().size() - 1);
        this.ledgerService.deleteTx(block.getHeader().getHeight());
        blockStorageService.delete(block.getHeader().getHash().getDigestHex());
        NulsContext.getInstance().setBestBlock(this.getBestBlock());
    }


    @Override
    public List<BlockHeader> getBlockHeaderList(long startHeight, long endHeight, long split) {
        return blockStorageService.getBlockHeaderList(startHeight, endHeight, split);
    }

    @Override
    public Page<BlockHeaderPo> getBlockHeaderList(String nodeAddress, int type, int pageNumber, int pageSize) {
        return blockStorageService.getBlocListByAddress(nodeAddress, type, pageNumber, pageSize);
    }

    @Override
    public Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize) {
        return blockStorageService.getBlockHeaderList(pageNumber, pageSize);
    }

    @Override
    public BlockHeader getBlockHeader(NulsDigestData hash) throws NulsException {
        String hashHex = hash.getDigestHex();
        BlockHeader header = blockManager.getBlockHeader(hashHex);
        if (null == header) {
            header = blockStorageService.getBlockHeader(hashHex);
        }
        return header;
    }

    @Override
    public long getPackingCount(String address) {
        return blockStorageService.getBlockCount(address, -1L, -1L,0L);
    }

    @Override
    public Map<String, Object> getSumTxCount(String address, long roundStart, long roundEnd) {
        return blockStorageService.getSumTxCount(address, roundStart, roundEnd);
    }

    @Override
    public Block getPreRoundFirstBlock(long roundIndex) {
        //todo block-》blockheader
        Long height = this.blockStorageService.getRoundFirstBlockHeight(roundIndex);
        if (null == height) {
            Block resultBlock = getBestBlock();
            Block preResultBlock = null;
            String hashHex = resultBlock.getHeader().getPreHash().getDigestHex();
            while (true) {
                BlockRoundData roundData = new BlockRoundData(resultBlock.getHeader().getExtend());
                if (roundData.getRoundIndex() == roundIndex && roundData.getPackingIndexOfRound() == 1) {
                    break;
                }
                if (roundData.getRoundIndex() < roundIndex) {
                    if (null != preResultBlock) {
                        resultBlock = preResultBlock;
                    }
                    break;
                }
                if (resultBlock.getHeader().getHeight() == 0) {
                    return resultBlock;
                }
                if (roundData.getRoundIndex() <= roundIndex) {
                    preResultBlock = resultBlock;
                }
                resultBlock = getBlock(hashHex);
                if (null == resultBlock) {
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "The block shouldn't be null");
                }
                hashHex = resultBlock.getHeader().getPreHash().getDigestHex();
            }
            return resultBlock;
        }
        return this.getBlock(height);
    }

    private void rollback(List<Transaction> txs, int max) {
        for (int x = 0; x <= max; x++) {
            Transaction tx = txs.get(x);
            try {
                ledgerService.rollbackTx(tx, null);
            } catch (NulsException e) {
                Log.error(e);
            }
        }

    }

    @Override
    public Block getBestBlock() {
        Block highestBlock = BlockManager.getInstance().getHighestBlock();
        if (null == highestBlock) {
            highestBlock = this.getLocalBestBlock();
        }
        return highestBlock;
    }

    @Override
    public void approvalBlock(String hash) {
        Block block = this.getBlock(hash);
        if (null == block) {
            Log.info("the block is null:" + block.getHeader().getHash());
            return;
        }
        blockManager.appravalBlock(block);
    }

    @Override
    public List<BlockHeaderPo> getBlockHashList(long start, long end) {
        return blockStorageService.getBlockHashList(start, end);
    }

    @Override
    public Block getBlockFromMyChain(long start) {
        return blockStorageService.getBlockFromMyChain(start);
    }

    @Override
    public Block getBlockFromMyChain(String hash) {
        return blockStorageService.getBlockFromMyChain(hash);
    }

    @Override
    public boolean rollbackBlock(Block rollBlock) {
        //TODO
        return false;
    }
}
