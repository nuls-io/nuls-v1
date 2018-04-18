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

package io.nuls.consensus.poc.manager;

import io.nuls.consensus.poc.cache.CacheLoader;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;

import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class CacheManager {

    private ChainManager chainManager;

    private CacheLoader cacheLoader = new CacheLoader();

    public CacheManager(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public void load() throws NulsException {

        //load storage data to memory

        List<BlockHeader> blockHeaderList = cacheLoader.loadBlockHeaders(200);
        List<Block> blockList = cacheLoader.loadBlocks(8);

        if(blockHeaderList == null || blockHeaderList.size() == 0 || blockList == null || blockList.size() == 0) {
            Log.error("load cache error ,not find the block info!");
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "load cache error ,not find the block info!");
        }
        List<Consensus<Agent>> agentList = cacheLoader.loadAgents();
        List<Consensus<Deposit>> depositList = cacheLoader.loadDepositList();
        List<PunishLogPo> yellowPunishList = cacheLoader.loadYellowPunishList();
        List<PunishLogPo> redPunishList = cacheLoader.loadRedPunishList();

        Chain masterChain = new Chain();

        masterChain.setBlockHeaderList(blockHeaderList);
        masterChain.setBlockList(blockList);

        masterChain.setStartBlockHeader(blockList.get(0).getHeader());
        masterChain.setEndBlockHeader(blockList.get(blockList.size() - 1).getHeader());
        masterChain.setAgentList(agentList);
        masterChain.setDepositList(depositList);
        masterChain.setYellowPunishList(yellowPunishList);
        masterChain.setRedPunishList(redPunishList);

        ChainContainer masterChainContainer = new ChainContainer(masterChain);

        chainManager.setMasterChain(masterChainContainer);

        chainManager.getMasterChain().resetRound(false);
    }

    public void reload() throws NulsException {
        clear();
        load();
    }

    public void clear() {
        chainManager.clear();
    }

    public CacheLoader getCacheLoader() {
        return cacheLoader;
    }

    public void setCacheLoader(CacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }
}
