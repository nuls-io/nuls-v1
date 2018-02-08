/**
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
package io.nuls.consensus.manager;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.BlockPersistenceThread;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.BaseThread;
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
public class ConsensusManager implements Runnable {
    private static ConsensusManager INSTANCE = new ConsensusManager();
    private BlockCacheManager blockCacheManager;
    private ConsensusCacheManager consensusCacheManager;
    private ConfirmingTxCacheManager confirmingTxCacheManager;
    private ReceivedTxCacheManager receivedTxCacheManager;
    private AccountService accountService;
    private boolean partakePacking = false;
    private List<String> seedNodeList;

    private PocMeetingRound currentRound;
    private ConsensusStatusInfo consensusStatusInfo;

    private ConsensusManager() {
    }

    public static ConsensusManager getInstance() {
        return INSTANCE;
    }

    private void loadConfigration() {
        NulsContext.getInstance().setGenesisBlock(GenesisBlock.getInstance());
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
        accountService = NulsContext.getServiceBean(AccountService.class);
        List<Account> list = this.accountService.getAccountList();
        boolean noneAccount = list == null || list.isEmpty();
        if (this.partakePacking && noneAccount) {
            Account account = this.accountService.createAccount();
            this.accountService.setDefaultAccount(account.getAddress().getBase58());
            NulsContext.LOCAL_ADDRESS_LIST.add(account.getAddress().getBase58());
        }
        blockCacheManager = BlockCacheManager.getInstance();
        blockCacheManager.init();
        consensusCacheManager = ConsensusCacheManager.getInstance();
        consensusCacheManager.init();
        confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();
        confirmingTxCacheManager.init();
        receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
        receivedTxCacheManager.init();
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS, "consensus-status-manager", this);
    }

    @Override
    public void run() {
        this.initConsensusStatusInfo();
    }

    public void initConsensusStatusInfo() {

        if (null == NulsContext.LOCAL_ADDRESS_LIST || NulsContext.LOCAL_ADDRESS_LIST.isEmpty()) {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            initConsensusStatusInfo();
            return;
        }
        List<Consensus<Agent>> agentList = consensusCacheManager.getCachedAgentList();
        ConsensusStatusInfo info = new ConsensusStatusInfo();
        for (String address : NulsContext.LOCAL_ADDRESS_LIST) {
            if (this.seedNodeList.contains(address)) {
                info.setAddress(address);
                info.setStatus(ConsensusStatusEnum.IN.getCode());
                break;
            }
            for (Consensus<Agent> agent : agentList) {
                if (agent.getExtend().getDelegateAddress().equals(address)) {
                    info.setAddress(address);
                    info.setStatus(agent.getExtend().getStatus());
                    if (ConsensusStatusEnum.IN.getCode() == info.getStatus()) {
                        break;
                    }
                }
            }
        }
        if (info.getAddress() != null) {
            this.consensusStatusInfo = info;
        }
    }

    public void joinMeeting() {
        if (null == this.consensusStatusInfo) {
            return;
        }
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

    public ConsensusStatusInfo getConsensusStatusInfo() {
        return consensusStatusInfo;
    }

    public void startMaintenanceWork() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        try {
            blockMaintenanceThread.checkGenesisBlock();
            blockMaintenanceThread.syncBlock();

        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS,
                    BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
        }
    }

    public void destroy() {
        blockCacheManager.clear();
        consensusCacheManager.clear();
        confirmingTxCacheManager.clear();
        receivedTxCacheManager.clear();
    }

    public void setCurrentRound(PocMeetingRound currentRound) {
        this.currentRound = currentRound;

    }

    public PocMeetingRound getCurrentRound() {
        return currentRound;
    }

    public boolean isPartakePacking() {
        return partakePacking;
    }

    public void exitMeeting() {
        TaskManager.stopThread(NulsConstant.MODULE_ID_CONSENSUS, BlockMaintenanceThread.THREAD_NAME);
    }

    public List<String> getSeedNodeList() {
        return seedNodeList;
    }
}
