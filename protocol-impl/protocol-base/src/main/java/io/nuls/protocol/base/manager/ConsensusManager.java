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
package io.nuls.protocol.base.manager;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.protocol.base.constant.PocConsensusConstant;
import io.nuls.protocol.base.entity.genesis.GenesisBlock;
import io.nuls.protocol.base.service.impl.BlockStorageService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class ConsensusManager {
    private static ConsensusManager INSTANCE = new ConsensusManager();

    private BlockStorageService blockStorageService = BlockStorageService.getInstance();
    private boolean partakePacking = false;
    private List<String> seedNodeList;
    private ScheduledThreadPoolExecutor threadPool;

    private ConsensusManager() {
    }

    public static ConsensusManager getInstance() {
        return INSTANCE;
    }

    private void loadConfigration() {
        Block bestBlock = null;
        Block genesisBlock = GenesisBlock.getInstance();
        NulsContext.getInstance().setGenesisBlock(genesisBlock);
        try {
            bestBlock = blockStorageService.getBlock(blockStorageService.getBestHeight());
        } catch (Exception e) {
            Log.error(e);
        }
        if (bestBlock == null) {
            bestBlock = genesisBlock;
        }
        NulsContext.getInstance().setBestBlock(bestBlock);

        partakePacking = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_PARTAKE_PACKING, false);
        seedNodeList = new ArrayList<>();
        Set<String> seedAddressSet = new HashSet<>();
        String addresses = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_SEED_NODES, "");
        if (StringUtils.isBlank(addresses)) {
            return;
        }
        String[] array = addresses.split(PocConsensusConstant.SEED_NODES_DELIMITER);
        if (null == array) {
            return;
        }
        for (String address : array) {
            seedAddressSet.add(address);
        }
        this.seedNodeList.addAll(seedAddressSet);
    }

    public void init() {
        loadConfigration();

    }


    public void startDownloadWork() {
        NulsContext.getServiceBean(DownloadService.class).start();
    }


    public void destroy() {
        threadPool.shutdown();
    }

    public boolean isPartakePacking() {
        boolean imIn = partakePacking;
        return imIn;
    }

    public List<String> getSeedNodeList() {
        return seedNodeList;
    }

}
