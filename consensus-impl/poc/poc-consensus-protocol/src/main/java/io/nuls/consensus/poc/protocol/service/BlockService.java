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
package io.nuls.consensus.poc.protocol.service;

import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.NulsDigestData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/10
 */
public interface BlockService {

    Block getGengsisBlock();

    Block getLocalBestBlock();

    BlockHeader getLocalBestBlockHeader();

    BlockHeader getBlockHeader(long height) throws NulsException;

    BlockHeader getBlockHeader(String hash) throws NulsException;

    Block getBlock(String hash);

    Block getBlock(long height);

    List<Block> getBlockList(long startHeight, long endHeight) throws NulsException;

    boolean saveBlock(Block block) throws IOException;

    boolean rollbackBlock(Block block) throws NulsException;

    Page<BlockHeaderPo> getBlockHeaderList(String nodeAddress, int type, int pageNumber, int pageSize);

    Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize);
    List<BlockHeaderPo> getBlockHeaderList(long startHeight,long endHeight);

    long getPackingCount(String address);

    Block getBestBlock();

    List<BlockHeaderPo> getBlockHashList(long start, long end);

    /**
     * get the block from approvingChain;
     * @param hash
     * @return
     */
    Block getBlockFromMyChain(String hash);

    Map<String,Object> getSumTxCount(String packingAddress, long startRoundIndex, long endRoundIndex);

    List<BlockHeaderPo> getBlockHeaderListByRound(long startRoundIndex, long endRoundIndex);

    long getPackingCount(String packingAddress, long roundStart, long roundEnd);
}
