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

package io.nuls.consensus.poc.cache;

import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.protocol.base.utils.BlockHeaderTool;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class CacheLoader {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);

    public List<Block> loadBlocks(int size) throws NulsException {

        Block block = blockService.getLocalBestBlock();

        long bestBlockHeight = block.getHeader().getHeight();

        long startHeight = bestBlockHeight - size + 1;
        if (startHeight < 0) {
            startHeight = 0;
        }

        List<Block> blockList = blockService.getBlockList(startHeight, bestBlockHeight);
        blockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return (int) (o1.getHeader().getHeight() - o2.getHeader().getHeight());
            }
        });

        return blockList;
    }

    public List<BlockHeader> loadBlockHeaders(int size) {
        Block block = blockService.getLocalBestBlock();
        if (null == block) {
            return new ArrayList<>();
        }
        BlockRoundData roundData = new BlockRoundData(block.getHeader().getExtend());


        List<BlockHeader> blockHeaderList = new ArrayList<>();

        List<BlockHeaderPo> list = blockService.getBlockHeaderListByRound(roundData.getRoundIndex() - size + 1, roundData.getRoundIndex());
        for (BlockHeaderPo blockHeaderPo : list) {
            try {
                blockHeaderList.add(BlockHeaderTool.fromPojo(blockHeaderPo));
            } catch (NulsException e) {
                Log.error(e);
            }
        }

        return blockHeaderList;
    }

    public List<Consensus<Agent>> loadAgents() {

        Block block = blockService.getLocalBestBlock();

        List<AgentPo> list = agentDataService.getAllList();

        List<Consensus<Agent>> agentList = new ArrayList<>();

        for (AgentPo agentPo : list) {
            agentList.add(ConsensusTool.fromPojo(agentPo));
        }

        return agentList;
    }

    public List<Consensus<Deposit>> loadDepositList() {
        Block block = blockService.getLocalBestBlock();

        List<DepositPo> list = depositDataService.getAllList();

        List<Consensus<Deposit>> depositList = new ArrayList<>();

        for (DepositPo depositPo : list) {
            depositList.add(ConsensusTool.fromPojo(depositPo));
        }

        return depositList;
    }

    public List<PunishLogPo> loadYellowPunishList() {
        List<PunishLogPo> list = punishLogDataService.getListByType(PunishType.YELLOW.getCode());
        return list;
    }

    public List<PunishLogPo> loadRedPunishList() {
        List<PunishLogPo> list = punishLogDataService.getListByType(PunishType.RED.getCode());
        return list;
    }
}
