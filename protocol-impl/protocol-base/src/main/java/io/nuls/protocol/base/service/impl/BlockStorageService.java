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
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockHeaderService;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.script.P2PKHScriptSig;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/10
 */
public class BlockStorageService {
    private static final BlockStorageService INSTANCE = new BlockStorageService();

    private BlockHeaderService headerDao = NulsContext.getServiceBean(BlockHeaderService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    private BlockStorageService() {
    }

    public static BlockStorageService getInstance() {
        return INSTANCE;
    }

    public Block getBlock(long height) throws Exception {

        BlockHeader header = getBlockHeader(height);
        if (null == header) {
            return null;
        }
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getTxList(height);
        } catch (Exception e) {
            Log.error(e);
        }
        if (header.getTxCount() != txList.size()) {
            Log.warn("block has wrong tx size!");
        }
        return fillBlock(header, txList);
    }

    public Block getBlock(String hash) throws Exception {
        BlockHeader header = getBlockHeader(hash);
        if (null == header) {
            return null;
        }
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getTxList(header.getHeight());
        } catch (Exception e) {
            Log.error(e);
        }
        return fillBlock(header, txList);
    }

    private Block fillBlock(BlockHeader header, List<Transaction> txList) {
        Block block = new Block();
        block.setTxs(txList);
        block.setHeader(header);
        return block;
    }


    public List<Block> getBlockList(long startHeight, long endHeight) throws NulsException {
        List<Block> blockList = new ArrayList<>();
        List<BlockHeaderPo> poList = headerDao.getHeaderList(startHeight, endHeight);
        List<Long> heightList = new ArrayList<>();
        if (!poList.isEmpty()) {
            List<Transaction> txList = null;
            try {
                txList = ledgerService.getTxList(startHeight, endHeight);
            } catch (Exception e) {
                Log.error(e);
            }
            Map<Long, List<Transaction>> txListGroup = txListGrouping(txList);
            for (BlockHeaderPo po : poList) {
                BlockHeader header = null;
                try {
                    header = fromPojo(po);
                } catch (NulsException e) {
                    throw e;
                }
                heightList.add(header.getHeight());
                blockList.add(fillBlock(header, txListGroup.get(header.getHeight())));
            }
        }
        if ((endHeight - startHeight + 1) > blockList.size()) {
            for (long i = startHeight; i <= endHeight; i++) {
                if (heightList.contains(i)) {
                    continue;
                }
                try {
                    blockList.add(this.getBlock(i));
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }
        return blockList;
    }

    private Map<Long, List<Transaction>> txListGrouping(List<Transaction> txList) {
        Map<Long, List<Transaction>> map = new HashMap<>();
        for (Transaction tx : txList) {
            List<Transaction> list = map.get(tx.getBlockHeight());
            if (null == list) {
                list = new ArrayList<>();
                map.put(tx.getBlockHeight(), list);
            }
            list.add(tx);
        }
        return map;
    }

    public BlockHeader getBlockHeader(long height) throws NulsException {
        BlockHeaderPo po = this.headerDao.getHeader(height);
        return fromPojo(po);
    }

    public BlockHeader getBlockHeader(String hash) throws NulsException {
        BlockHeaderPo po = this.headerDao.getHeader(hash);
        return fromPojo(po);
    }

    public long getBestHeight() {
        return headerDao.getBestHeight();
    }

    public void save(Block block) {
        BlockHeader header = block.getHeader();
        header.setSize(block.size());
        try {
            int count = headerDao.save(toPojo(header));
            if (count == 0) {
                Log.error("=======================");
                throw new NulsRuntimeException(ErrorCode.FAILED, "保存出错，高度：" + header.getHeight());
            }
        } catch (Exception e) {
            Log.error(e);
            Log.error(e);
        }
    }

    public void delete(String hash) {
        headerDao.delete(hash);
    }

    public List<BlockHeaderPo> getBlockHeaderList(long startHeight, long endHeight) {
        return this.headerDao.getHeaderList(startHeight, endHeight);
    }

    public Page<BlockHeaderPo> getBlocListByAddress(String nodeAddress, int type, int pageNumber, int pageSize) {
        return headerDao.getBlockListByAddress(nodeAddress, type, pageNumber, pageSize);
    }

    public Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize) {
        return headerDao.getBlockHeaderList(pageNumber, pageSize);
    }


    public long getBlockCount(String address, long roundStart, long roundEnd, long startHeight) {
        return this.headerDao.getCount(address, roundStart, roundEnd, startHeight);
    }

    public Map<String, Object> getSumTxCount(String address, long roundStart, long roundEnd) {
        return headerDao.getSumTxCount(address, roundStart, roundEnd);
    }

    public Long getRoundFirstBlockHeight(long roundIndex) {
        return this.headerDao.getRoundFirstBlockHeight(roundIndex);
    }

    public List<BlockHeaderPo> getBlockHashList(long start, long end) {
        return this.headerDao.getBlockHashList(start, end);
    }

    public static final BlockHeaderPo toPojo(BlockHeader header) {
        BlockHeaderPo po = new BlockHeaderPo();
        po.setTxCount(header.getTxCount());
        po.setPreHash(header.getPreHash().getDigestHex());
        po.setMerkleHash(header.getMerkleHash().getDigestHex());
        po.setHeight(header.getHeight());
        po.setCreateTime(header.getTime());
        po.setHash(header.getHash().getDigestHex());
        po.setSize(header.getSize());
        if (null != header.getScriptSig()) {
            try {
                po.setScriptSig(header.getScriptSig().serialize());
            } catch (IOException e) {
                Log.error(e);
            }
        }
        po.setTxCount(header.getTxCount());
        po.setConsensusAddress(Address.fromHashs(header.getPackingAddress()).getBase58());
        po.setExtend(header.getExtend());
        BlockRoundData data = new BlockRoundData();
        try {
            data.parse(header.getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        po.setRoundIndex(data.getRoundIndex());
        return po;
    }


    public static final BlockHeader fromPojo(BlockHeaderPo po) throws NulsException {
        if (null == po) {
            return null;
        }
        BlockHeader header = new BlockHeader();
        header.setHash(NulsDigestData.fromDigestHex(po.getHash()));
        header.setMerkleHash(NulsDigestData.fromDigestHex(po.getMerkleHash()));
        header.setPackingAddress(Address.fromHashs(po.getConsensusAddress()).getHash());
        header.setTxCount(po.getTxCount());
        header.setPreHash(NulsDigestData.fromDigestHex(po.getPreHash()));
        header.setTime(po.getCreateTime());
        header.setHeight(po.getHeight());
        header.setExtend(po.getExtend());
        header.setSize(po.getSize());
        header.setScriptSig((new NulsByteBuffer(po.getScriptSig()).readNulsData(new P2PKHScriptSig())));
        return header;
    }

}
