/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.cache;

import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.po.PunishLogPo;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class CacheLoader {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    public List<Block> loadBlocks(int size) throws NulsException {

        List<Block> blockList = new ArrayList<>();

        Block block = blockService.getBestBlock().getData();

        if(null == block){
            return blockList;
        }

        for(int i = size ; i >= 0 ; i --) {

            if(block == null) {
                break;
            }

            blockList.add(block);

            if(block.getHeader().getHeight() == 0L) {
                break;
            }

            NulsDigestData preHash = block.getHeader().getPreHash();
            block = blockService.getBlock(preHash).getData();
            if(block == null || block.getHeader().getHeight() == 0L) {
                break;
            }
        }

        return blockList;
    }

    public List<BlockHeader> loadBlockHeaders(int size) {

        List<BlockHeader> blockHeaderList = new ArrayList<>();

        BlockHeader blockHeader = blockService.getBestBlockHeader().getData();

        if(null == blockHeader){
            return blockHeaderList;
        }

        for(int i = size ; i >= 0 ; i --) {

            if(blockHeader == null) {
                break;
            }

            blockHeaderList.add(blockHeader);

            if(blockHeader.getHeight() == 0L) {
                break;
            }

            NulsDigestData preHash = blockHeader.getPreHash();
            blockHeader = blockService.getBlockHeader(preHash).getData();
        }

        return blockHeaderList;
    }

    public List<Transaction<Agent>> loadAgents() {

        List<Transaction<Agent>> agentList = new ArrayList<>();

        return agentList;
    }

    public List<Transaction<Deposit>> loadDepositList() {

        List<Transaction<Deposit>> depositList = new ArrayList<>();

        return depositList;
    }

    public List<PunishLogPo> loadYellowPunishList() {
        List<PunishLogPo> list = new ArrayList<>();
        return list;
    }

    public List<PunishLogPo> loadRedPunishList() {
        List<PunishLogPo> list = new ArrayList<>();
        return list;
    }
}
