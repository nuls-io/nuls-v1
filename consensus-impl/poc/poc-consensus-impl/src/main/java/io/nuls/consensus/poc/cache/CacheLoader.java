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

import io.nuls.consensus.poc.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.member.Deposit;
import io.nuls.protocol.entity.Consensus;
import io.nuls.protocol.intf.BlockService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class CacheLoader {
    //TODO

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

        return blockList;
    }

    public List<BlockHeader> loadBlockHeaders(int size) {

        //TODO

        Block block = blockService.getLocalBestBlock();

        long bestBlockHeight = block.getHeader().getHeight();

        List<BlockHeader> blockHeaderList = new ArrayList<>();
        Page<BlockHeaderPo> page = blockService.getBlockHeaderList(1, (int) bestBlockHeight);

        List<BlockHeaderPo> list = page.getList();
        for(BlockHeaderPo blockHeaderPo : list) {
            try {
                blockHeaderList.add(ConsensusTool.fromPojo(blockHeaderPo));
            } catch (NulsException e) {
                e.printStackTrace();
            }
        }

        Collections.reverse(blockHeaderList);

        return blockHeaderList;
    }

    public List<Consensus<Agent>> loadAgents() {

        Block block = blockService.getLocalBestBlock();

        long bestBlockHeight = block.getHeader().getHeight();

        List<AgentPo> list = agentDataService.getAllList(bestBlockHeight);

        List<Consensus<Agent>> agentList = new ArrayList<>();

        for(AgentPo agentPo : list) {
            agentList.add(ConsensusTool.fromPojo(agentPo));
        }

        return agentList;
    }

    public List<Consensus<Deposit>> loadDepositList() {
        Block block = blockService.getLocalBestBlock();

        long bestBlockHeight = block.getHeader().getHeight();

        List<DepositPo> list = depositDataService.getAllList(bestBlockHeight);

        List<Consensus<Deposit>> depositList = new ArrayList<>();

        for(DepositPo depositPo : list) {
            depositList.add(ConsensusTool.fromPojo(depositPo));
        }

        return depositList;
    }

    public List<PunishLogPo> loadYellowPunishList() {
        //TODO
//
//        List<PunishLogPo> list = punishLogDataService.getList();
//
//        return list;
        return new ArrayList<>();
    }

    public List<PunishLogPo> loadRedPunishList() {
        return new ArrayList<>();
    }
}
