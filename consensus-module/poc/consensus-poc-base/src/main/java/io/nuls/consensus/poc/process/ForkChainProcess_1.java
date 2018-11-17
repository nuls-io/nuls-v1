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

package io.nuls.consensus.poc.process;

import com.google.common.primitives.UnsignedBytes;
import io.nuls.consensus.poc.constant.ConsensusStatus;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.context.ConsensusStatusContext;
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.ChainLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.*;

/**
 * @author ln
 */
public class ForkChainProcess_1 {

    private ChainManager chainManager;

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private long time = 0L;
    private long lastClearTime = 0L;

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private ContractService contractService = NulsContext.getServiceBean(ContractService.class);
    private TransactionService tansactionService = NulsContext.getServiceBean(TransactionService.class);

    private NulsProtocolProcess nulsProtocolProcess = NulsProtocolProcess.getInstance();

    public ForkChainProcess_1(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public boolean doProcess() throws IOException, NulsException {
        if (ConsensusStatusContext.getConsensusStatus().ordinal() < ConsensusStatus.RUNNING.ordinal()) {
            return false;
        }
        Lockers.CHAIN_LOCK.lock();
        try {
            printChainStatusLog();
            ChainContainer masterChain = chainManager.getMasterChain();
            if (null == masterChain) {
                return false;
            }
            // Monitor the status of the orphan chain, if it is available, join the verification chain
            // 监控孤立链的状态，如果有可连接的，则加入验证链里面
            monitorOrphanChains();

            BlockHeader masterBestHeader = masterChain.getBestBlock().getHeader();
            //寻找可切换的链
            ChainContainer wantToChangeChain = findWantToChangeChain(masterBestHeader);
            if (wantToChangeChain != null) {

            }
            clearExpiredChain();
        } finally {
            Lockers.CHAIN_LOCK.unlock();
        }
        return true;
    }

    /***
     * 查询当前所有分叉链里，是否存在可切换为主链的
     * 可切换主链规则：
     *    1.高度和主链相同，出块人为同一个人，说明有出块节点在同一时间出了两个块，我们统一选择hash大的那个作为主链
     *    2.当前有分叉链比主网高度高出3个块
     * @param masterBestHeader
     * @return
     */
    private ChainContainer findWantToChangeChain(BlockHeader masterBestHeader) {
        ChainContainer changeContainer = null;
        long newestBlockHeight = chainManager.getBestBlockHeight() + PocConsensusConstant.CHANGE_CHAIN_BLOCK_DIFF_COUNT;

        Iterator<ChainContainer> iterator = chainManager.getChains().iterator();
        BlockHeader forkChainBestHeader = null;
        byte[] rightHash = null;
        while (iterator.hasNext()) {
            ChainContainer forkChain = iterator.next();
            //删除格式错误的分叉链
            if (forkChain.getChain() == null || forkChain.getChain().getStartBlockHeader() == null || forkChain.getChain().getEndBlockHeader() == null) {
                iterator.remove();
                continue;
            }
            forkChainBestHeader = forkChain.getChain().getEndBlockHeader();
            //同一高度有人出了两个块，判断哪条链的块hash值更小，如果分叉链的块hash值更小，则切换到分叉链
            if (masterBestHeader.getHeight() == forkChainBestHeader.getHeight() && masterBestHeader.getPackingAddressStr().equals(forkChainBestHeader.getPackingAddressStr())) {
                byte[] smallHash = compareHashAndGetSmall(masterBestHeader.getHash().getDigestBytes(), forkChainBestHeader.getHash().getDigestBytes());
                if (!ArraysTool.arrayEquals(smallHash, masterBestHeader.getHash().getDigestBytes())) {
                    changeContainer = forkChain;
                    break;
                }
            }

            if (forkChainBestHeader.getHeight() > newestBlockHeight
                    || (forkChainBestHeader.getHeight() == newestBlockHeight && forkChainBestHeader.getTime() < masterBestHeader.getTime())) {
                changeContainer = forkChain;
                newestBlockHeight = forkChainBestHeader.getHeight();
                rightHash = forkChainBestHeader.getHash().getDigestBytes();
            }
        }

        if (changeContainer != null) {
            Log.info("-+-+-+-+-+-+-+-+- Change chain with the same height but different hash block -+-+-+-+-+-+-+-+-");
            Log.info("-+-+-+-+-+-+-+-+- height: " + newestBlockHeight + ", Right hash：" + Hex.encode(rightHash));
            /** ******************************************************************************************************** */
            try {
                Log.info("");
                Log.info("****************************************************");
                Log.info("准备开始切换链，获取当前bestblock, height:{}，- hash{}", chainManager.getBestBlock().getHeader().getHeight(), chainManager.getBestBlock().getHeader().getHash());
                Log.info("****************************************************");
                Log.info("");

            } catch (Exception e) {
                e.printStackTrace();
            }
            /** ******************************************************************************************************** */
        }
        return changeContainer;
    }


    /**
     * Monitor the orphan chain, if there is a connection with the main chain or the forked chain, the merged chain
     * <p>
     * 监控孤立链，如果有和主链或者分叉链连上的情况，则合并链
     */
    private void monitorOrphanChains() {
        List<ChainContainer> orphanChains = chainManager.getOrphanChains();

        Iterator<ChainContainer> iterator = orphanChains.iterator();
        while (iterator.hasNext()) {
            ChainContainer orphanChain = iterator.next();
            if (checkOrphanChainHasConnection(orphanChain)) {
                iterator.remove();
            }
        }
    }

    private boolean checkOrphanChainHasConnection(ChainContainer orphanChain) {
        // Determine whether the orphan chain is connected to the main chain
        // 判断该孤立链是否和主链相连
        BlockHeader startBlockHeader = orphanChain.getChain().getStartBlockHeader();

        List<BlockHeader> blockHeaderList = chainManager.getMasterChain().getChain().getAllBlockHeaderList();

        int count = blockHeaderList.size() > PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT ? PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT : blockHeaderList.size();
        for (int i = blockHeaderList.size() - 1; i >= blockHeaderList.size() - count; i--) {
            BlockHeader header = blockHeaderList.get(i);
            if (startBlockHeader.getPreHash().equals(header.getHash()) && startBlockHeader.getHeight() == header.getHeight() + 1) {
                //yes connectioned
                orphanChain.getChain().setPreChainId(chainManager.getMasterChain().getChain().getId());

                chainManager.getChains().add(orphanChain);

                ChainLog.debug("discover the OrphanChain {} : start {} - {} , end {} - {} , connection the master chain of {} - {} - {}, move into the fork chians", orphanChain.getChain().getId(), startBlockHeader.getHeight(), startBlockHeader.getHash().getDigestHex(), orphanChain.getChain().getEndBlockHeader().getHeight(), orphanChain.getChain().getEndBlockHeader().getHash(), chainManager.getMasterChain().getChain().getId(), chainManager.getMasterChain().getChain().getBestBlock().getHeader().getHeight(), chainManager.getMasterChain().getChain().getBestBlock().getHeader().getHash());

                return true;
            } else if (startBlockHeader.getHeight() > header.getHeight()) {
                break;
            }
        }

        // Determine whether the lone chain is connected to the forked chain to be verified
        // 判断该孤链是否和待验证的分叉链相连
        for (ChainContainer forkChain : chainManager.getChains()) {

            Chain chain = forkChain.getChain();

            if (startBlockHeader.getHeight() > chain.getEndBlockHeader().getHeight() + 1 || startBlockHeader.getHeight() <= chain.getStartBlockHeader().getHeight()) {
                continue;
            }

            blockHeaderList = chain.getAllBlockHeaderList();

            for (int i = 0; i < blockHeaderList.size(); i++) {
                BlockHeader header = blockHeaderList.get(i);
                if (startBlockHeader.getPreHash().equals(header.getHash()) && startBlockHeader.getHeight() == header.getHeight() + 1) {
                    //yes connectioned
                    orphanChain.getChain().setPreChainId(chain.getPreChainId());
                    orphanChain.getChain().initData(chain.getStartBlockHeader(), blockHeaderList.subList(0, i + 1), chain.getAllBlockList().subList(0, i + 1));
                    chainManager.getChains().add(orphanChain);

                    if (i == blockHeaderList.size() - 1) {
                        chainManager.getChains().remove(forkChain);
                    }

                    ChainLog.debug("discover the OrphanChain {} : start {} - {} , end {} - {} , connection the fork chain of : start {} - {} , end {} - {}, move into the fork chians", orphanChain.getChain().getId(), startBlockHeader.getHeight(), startBlockHeader.getHash().getDigestHex(), orphanChain.getChain().getEndBlockHeader().getHeight(), orphanChain.getChain().getEndBlockHeader().getHash(), chainManager.getMasterChain().getChain().getId(), chain.getStartBlockHeader().getHeight(), chain.getStartBlockHeader().getHash(), chain.getEndBlockHeader().getHeight(), chain.getEndBlockHeader().getHash());

                    return true;
                } else if (startBlockHeader.getHeight() == header.getHeight() + 1) {
                    break;
                }
            }
        }

        // Determine whether the orphan chains are connected
        // 判断孤立链之间是否相连
        for (ChainContainer orphan : chainManager.getOrphanChains()) {
            if (orphan.getChain().getEndBlockHeader().getHash().equals(orphanChain.getChain().getStartBlockHeader().getPreHash()) &&
                    orphan.getChain().getEndBlockHeader().getHeight() + 1 == orphanChain.getChain().getStartBlockHeader().getHeight()) {
                Chain chain = orphan.getChain();
                chain.initData(orphanChain.getChain().getEndBlockHeader(), orphanChain.getChain().getAllBlockHeaderList(), orphanChain.getChain().getAllBlockList());
                return true;
            }
        }

        return false;
    }


    /**
     * 当两个高度一致的块hash不同时，排序统一选取前面一个hash为正确的
     */
    private byte[] compareHashAndGetSmall(byte[] hash1, byte[] hash2) {
        Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
        if (comparator.compare(hash1, hash2) <= 0) {
            return hash1;
        }
        return hash2;
    }

    private void printChainStatusLog() {
        if (chainManager.getMasterChain() == null || chainManager.getMasterChain().getChain() == null || chainManager.getMasterChain().getChain().getEndBlockHeader() == null) {
            return;
        }

        if (time == 0L) {
            printLog();
        } else if (System.currentTimeMillis() - time > 5 * 60 * 1000L) {
            printLog();
        }
    }

    private void printLog() {
        time = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();

        sb.append("=========================\n");

        sb.append("Master Chain Status : \n");
        sb.append(getChainStatus(chainManager.getMasterChain()));

        sb.append("\n");

        List<ChainContainer> chains = chainManager.getChains();

        if (chains != null && chains.size() > 0) {
            sb.append("fork chains : \n");
            for (ChainContainer chain : chains) {
                sb.append(getChainStatus(chain));
            }
            sb.append("\n");
        }

        List<ChainContainer> iss = chainManager.getOrphanChains();

        if (iss != null && iss.size() > 0) {
            sb.append("orphan chains : \n");
            for (ChainContainer chain : iss) {
                sb.append(getChainStatus(chain));
            }
            sb.append("\n");
        }

        ChainLog.debug(sb.toString());
    }

    private String getChainStatus(ChainContainer chain) {
        StringBuilder sb = new StringBuilder();

        if (chain == null || chain.getChain() == null) {
            return sb.toString();
        }

        sb.append("id: " + chain.getChain().getId() + "\n");

        if (chain.getChain().getStartBlockHeader() == null) {
            sb.append("start Block Header is null \n");
        } else {
            sb.append("start height : " + chain.getChain().getStartBlockHeader().getHeight() + " \n");
            sb.append("start hash : " + chain.getChain().getStartBlockHeader().getHash() + " \n");
        }
        if (chain.getChain().getEndBlockHeader() == null) {
            sb.append("end Block Header is null \n");
        } else {
            sb.append("end height : " + chain.getChain().getEndBlockHeader().getHeight() + " \n");
            sb.append("end hash : " + chain.getChain().getEndBlockHeader().getHash() + " \n");
        }

        List<BlockHeader> blockHeaderList = chain.getChain().getAllBlockHeaderList();

        if (blockHeaderList != null && blockHeaderList.size() > 0) {
            sb.append("start blockHeaders height : " + blockHeaderList.get(0).getHeight() + " \n");
            sb.append("end blockHeaders height : " + blockHeaderList.get(blockHeaderList.size() - 1).getHeight() + " \n");
            sb.append("start blockHeaders hash : " + blockHeaderList.get(0).getHash() + " \n");
            sb.append("end blockHeaders hash : " + blockHeaderList.get(blockHeaderList.size() - 1).getHash() + " \n");
        }

        List<Block> block = chain.getChain().getAllBlockList();

        if (block != null && block.size() > 0) {
            sb.append("start blocks height : " + block.get(0).getHeader().getHeight() + " \n");
            sb.append("end blocks height : " + block.get(block.size() - 1).getHeader().getHeight() + " \n");
            sb.append("start blocks hash : " + block.get(0).getHeader().getHash() + " \n");
            sb.append("end blocks hash : " + block.get(block.size() - 1).getHeader().getHash() + " \n");
        }
        sb.append("\n");

        return sb.toString();
    }

    protected void clearExpiredChain() {
        if (TimeService.currentTimeMillis() - lastClearTime < PocConsensusConstant.CLEAR_INTERVAL_TIME) {
            return;
        }
        lastClearTime = TimeService.currentTimeMillis();
        //clear the master data
        clearMasterDatas();

        //clear the expired chain
        long bestHeight = chainManager.getBestBlockHeight();

        Iterator<ChainContainer> it = chainManager.getChains().iterator();
        while (it.hasNext()) {
            ChainContainer chain = it.next();
            if (checkChainIsExpired(chain, bestHeight)) {
                it.remove();
            }
        }

        it = chainManager.getOrphanChains().iterator();
        while (it.hasNext()) {
            ChainContainer orphanChain = it.next();
            if (checkChainIsExpired(orphanChain, bestHeight)) {
                it.remove();
            }
        }
    }

    private boolean checkChainIsExpired(ChainContainer orphanChain, long bestHeight) {
        if (bestHeight - orphanChain.getChain().getEndBlockHeader().getHeight() > PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT) {
            return true;
        }
        return false;
    }

    private void clearMasterDatas() {
        clearMasterChainRound();
        clearMasterChainData();
    }

    private void clearMasterChainData() {
        Chain masterChain = chainManager.getMasterChain().getChain();
        long bestHeight = masterChain.getEndBlockHeader().getHeight();

        List<Agent> agentList = masterChain.getAgentList();
        List<Deposit> depositList = masterChain.getDepositList();

        Iterator<Agent> ait = agentList.iterator();
        while (ait.hasNext()) {
            Agent agent = ait.next();
            if (agent.getDelHeight() > 0L && (bestHeight - 1000) > agent.getDelHeight()) {
                ait.remove();
            }
        }

        Iterator<Deposit> dit = depositList.iterator();
        while (dit.hasNext()) {
            Deposit deposit = dit.next();
            if (deposit.getDelHeight() > 0L && (bestHeight - 1000) > deposit.getDelHeight()) {
                dit.remove();
            }
        }

        BlockExtendsData roundData = new BlockExtendsData(chainManager.getBestBlock().getHeader().getExtend());

        List<PunishLogPo> yellowList = masterChain.getYellowPunishList();
        Iterator<PunishLogPo> yit = yellowList.iterator();
        while (yit.hasNext()) {
            PunishLogPo punishLog = yit.next();
            if (punishLog.getRoundIndex() < roundData.getPackingIndexOfRound() - PocConsensusConstant.INIT_HEADERS_OF_ROUND_COUNT) {
                yit.remove();
            }
        }
    }

    private void clearMasterChainRound() {
        chainManager.getMasterChain().clearRound(PocConsensusConstant.CLEAR_MASTER_CHAIN_ROUND_COUNT);
    }
}