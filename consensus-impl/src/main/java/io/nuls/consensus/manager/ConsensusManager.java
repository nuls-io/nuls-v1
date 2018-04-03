/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.manager;

import io.nuls.consensus.cache.manager.block.BlockCacheBuffer;
import io.nuls.consensus.cache.manager.block.ConfirmingBlockCacheManager;
import io.nuls.consensus.cache.manager.block.TemporaryCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.consensus.service.impl.BlockStorageService;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.BlockPersistenceThread;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class ConsensusManager {
    private static ConsensusManager INSTANCE = new ConsensusManager();
    private TemporaryCacheManager temporaryCacheManager;
    private BlockCacheBuffer blockCacheBuffer;
    private ConfirmingBlockCacheManager confirmingBlockCacheManager;

    private BlockManager blockCacheManager;
    private ConsensusCacheManager consensusCacheManager;
    private ConfirmingTxCacheManager confirmingTxCacheManager;
    private ReceivedTxCacheManager receivedTxCacheManager;
    private OrphanTxCacheManager orphanTxCacheManager;
    private BlockStorageService blockStorageService = BlockStorageService.getInstance();
    private boolean partakePacking = false;
    private List<String> seedNodeList;

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
        NulsContext.getInstance().setBestBlock1(bestBlock);

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
        if (this.partakePacking) {
            //todo
        }
        this.temporaryCacheManager = TemporaryCacheManager.getInstance();
        this.temporaryCacheManager.init();
        this.blockCacheBuffer = BlockCacheBuffer.getInstance();
        this.blockCacheBuffer.init();
        this.confirmingBlockCacheManager = ConfirmingBlockCacheManager.getInstance();
        this.confirmingBlockCacheManager.init();


        blockCacheManager = BlockManager.getInstance();
        blockCacheManager.init();


        consensusCacheManager = ConsensusCacheManager.getInstance();
        confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
        confirmingTxCacheManager.init();
        receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
        receivedTxCacheManager.init();
        orphanTxCacheManager = OrphanTxCacheManager.getInstance();
        orphanTxCacheManager.init();
    }

    public void joinConsensusMeeting() {
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS,
                ConsensusMeetingRunner.THREAD_NAME,
                ConsensusMeetingRunner.getInstance());
    }

    /**
     * data storage
     */
    public void startPersistenceWork() {
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS, BlockPersistenceThread.THREAD_NAME, BlockPersistenceThread.getInstance());
    }

    public void startMaintenanceWork() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        try {
            blockMaintenanceThread.checkGenesisBlock();
            blockMaintenanceThread.syncBlock();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    public void destroy() {
        blockCacheManager.clear();
        temporaryCacheManager.clear();
        consensusCacheManager.clear();
        confirmingTxCacheManager.clear();
        receivedTxCacheManager.clear();
        orphanTxCacheManager.clear();
    }

    public boolean isPartakePacking() {
        boolean imIn = partakePacking;
        return imIn;
    }

    public List<String> getSeedNodeList() {
        return seedNodeList;
    }
}
