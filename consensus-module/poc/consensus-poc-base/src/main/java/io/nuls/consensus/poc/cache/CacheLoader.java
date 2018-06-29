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

import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.util.AgentComparator;
import io.nuls.consensus.poc.protocol.util.DepositComparator;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.consensus.poc.storage.utils.PunishLogComparator;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 系统启动时加载缓存的处理器
 * The cmd that loads the cache when the system starts.
 *
 * @author ln
 */
public class CacheLoader {
    /**
     * 区块服务
     */
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private AgentStorageService agentStorageService = NulsContext.getServiceBean(AgentStorageService.class);
    private DepositStorageService depositStorageService = NulsContext.getServiceBean(DepositStorageService.class);


    /**
     * 从数据存储中加载指定个数的最新块
     * Loads the latest block of the specified number from the data store.
     *
     * @param size 加载数量/load count
     * @return 区块列表/block list
     */
    public List<Block> loadBlocks(int size) {

        List<Block> blockList = new ArrayList<>();

        Block block = blockService.getBestBlock().getData();

        if (null == block) {
            return blockList;
        }

        for (int i = size; i >= 0; i--) {

            if (block == null) {
                break;
            }

            blockList.add(0, block);

            if (block.getHeader().getHeight() == 0L) {
                break;
            }

            NulsDigestData preHash = block.getHeader().getPreHash();
            block = blockService.getBlock(preHash).getData();
            if (block == null || block.getHeader().getHeight() == 0L) {
                break;
            }
        }

        return blockList;
    }

    /**
     *
     * @param size
     * @return
     */
    public List<BlockHeader> loadBlockHeaders(int size) {

        List<BlockHeader> blockHeaderList = new ArrayList<>();

        BlockHeader blockHeader = blockService.getBestBlockHeader().getData();

        if (null == blockHeader) {
            return blockHeaderList;
        }
        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());
        long breakRoundIndex = roundData.getRoundIndex() - size;
        while (true) {
            if (blockHeader == null) {
                break;
            }

            blockHeaderList.add(0, blockHeader);

            if (blockHeader.getHeight() == 0L) {
                break;
            }

            NulsDigestData preHash = blockHeader.getPreHash();
            blockHeader = blockService.getBlockHeader(preHash).getData();
            BlockRoundData blockRoundData = new BlockRoundData(blockHeader.getExtend());
            if (blockRoundData.getRoundIndex() <= breakRoundIndex) {
                break;
            }
        }

        return blockHeaderList;
    }

    public List<Agent> loadAgents() {

        List<Agent> agentList = new ArrayList<>();
        List<AgentPo> poList = this.agentStorageService.getList();
        for (AgentPo po : poList) {
            Agent agent = PoConvertUtil.poToAgent(po);
            agentList.add(agent);
        }
        Collections.sort(agentList, new AgentComparator());
        return agentList;
    }

    public List<Deposit> loadDepositList() {
        List<Deposit> depositList = new ArrayList<>();
        List<DepositPo> poList = depositStorageService.getList();
        for (DepositPo po : poList) {
            depositList.add(PoConvertUtil.poToDeposit(po));
        }
        Collections.sort(depositList, new DepositComparator());
        return depositList;
    }

    public List<PunishLogPo> loadYellowPunishList(List<PunishLogPo> allPunishList, int roundSize) {
        List<PunishLogPo> list = new ArrayList<>();
        BlockHeader blockHeader = blockService.getBestBlockHeader().getData();

        if (null == blockHeader) {
            return list;
        }
        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());
        long breakRoundIndex = roundData.getRoundIndex() - roundSize;
        for (PunishLogPo po : allPunishList) {
            if (po.getType() == PunishType.RED.getCode()) {
                continue;
            }
            if (po.getRoundIndex() <= breakRoundIndex) {
                continue;
            }
            list.add(po);
        }
        Collections.sort(list, new PunishLogComparator());
        return list;
    }

    public List<PunishLogPo> loadRedPunishList(List<PunishLogPo> allPunishList) {
        List<PunishLogPo> list = new ArrayList<>();
        for (PunishLogPo po : allPunishList) {
            if (po.getType() == PunishType.RED.getCode()) {
                list.add(po);
            }
        }
        Collections.sort(list, new PunishLogComparator());
        return list;
    }
}
