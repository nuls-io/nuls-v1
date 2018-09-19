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
 *
 */

package io.nuls.consensus.poc.block.validator;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.Evidence;
import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.storage.service.BifurcationEvidenceStorageService;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.service.BlockService;

import java.io.IOException;
import java.util.*;

/**
 * @author: Niels Wang
 */
public class BifurcationUtil {

    private static final BifurcationUtil INSTANCE = new BifurcationUtil();

    private static String CLASS_NAME = BifurcationUtil.class.getName();

    /**
     * 记录出块地址PackingAddress，同一个高度发出了两个不同的块的证据
     * 下一轮正常则清零， 连续3轮将会被红牌惩罚
     */
    private Map<String, List<Evidence>> bifurcationEvidenceMap = new HashMap<>();

    private BifurcationEvidenceStorageService bifurcationEvidenceStorageService = NulsContext.getServiceBean(BifurcationEvidenceStorageService.class);

    private BifurcationUtil() {
    }

    public void setBifurcationEvidenceMap(Map<String, List<Evidence>> bifurcationEvidenceMap) {
        this.bifurcationEvidenceMap = bifurcationEvidenceMap;
    }

    public static BifurcationUtil getInstance() {
        return INSTANCE;
    }

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    public ValidateResult validate(BlockHeader header) {
        ValidateResult result = ValidateResult.getSuccessResult();
        if (header.getHeight() == 0L) {
            return result;
        }
        if (header.getHeight() > NulsContext.getInstance().getBestHeight()) {
            return result;
        }

        BlockHeader otherBlockHeader = blockService.getBlockHeader(header.getHeight()).getData();
        if (null != otherBlockHeader && !otherBlockHeader.getHash().equals(header.getHash()) && Arrays.equals(otherBlockHeader.getPackingAddress(), header.getPackingAddress())) {
            List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getDelHeight() > 0) {
                    continue;
                }
                if (Arrays.equals(a.getPackingAddress(), header.getPackingAddress())) {
                    agent = a;
                    break;
                }
            }
            if (null == agent) {
                return result;
            }
            recordEvidence(agent, header, otherBlockHeader);
            if(!isRedPunish(agent)){
                return result;
            }
            RedPunishTransaction redPunishTransaction = new RedPunishTransaction();
            RedPunishData redPunishData = new RedPunishData();
            redPunishData.setAddress(agent.getAgentAddress());

            long headerTime = 0;
            try {
                //连续3轮 每一轮两个区块头作为证据 一共 3 * 2 个区块头作为证据
                byte[][] headers = new byte[NulsContext.REDPUNISH_BIFURCATION * 2][];
                List<Evidence> list = bifurcationEvidenceMap.get(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
                for (int i = 0; i < list.size() && i < NulsContext.REDPUNISH_BIFURCATION;i++){
                    Evidence evidence = list.get(i);
                    int s = i * 2;
                    headers[s] = evidence.getBlockHeader1().serialize();
                    headers[++s] = evidence.getBlockHeader2().serialize();
                    if(s == headers.length - 1){
                        headerTime = evidence.getBlockHeader1().getTime();
                    }
                }
                redPunishData.setEvidence(ArraysTool.concatenate(headers));
            } catch (Exception e) {
                Log.error(e);
                return result;
            }
            redPunishData.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
            redPunishTransaction.setTxData(redPunishData);
            CoinData coinData = null;
            try {
                coinData = ConsensusTool.getStopAgentCoinData(agent, headerTime + PocConsensusConstant.RED_PUNISH_LOCK_TIME);
            } catch (IOException e) {
                Log.error(e);
                return result;
            }
            redPunishTransaction.setCoinData(coinData);
            try {
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            } catch (IOException e) {
                Log.error(e);
                return result;
            }
            TxMemoryPool.getInstance().add(redPunishTransaction, false);
            return result;
        }

        return result;
    }

    /**
     * 统计并验证分叉出块地址的证据，如果是连续的分叉则保存到证据集合中，不是连续的就清空
     * @param agent 分叉的出块地址所有者节点
     * @param header 新收到的区块头
     * @param otherBlockHeader 本地当前以保存的最新区块头
     * @return
     */
    private void recordEvidence(Agent agent, BlockHeader header, BlockHeader otherBlockHeader){
        //验证出块地址PackingAddress，记录分叉的连续次数，如达到连续3轮则红牌惩罚
        String packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress());
        BlockExtendsData extendsData = new BlockExtendsData(header.getExtend());
        Evidence evidence = new Evidence(extendsData.getRoundIndex(),header, otherBlockHeader);
        if(!bifurcationEvidenceMap.containsKey(packingAddress)){
            List<Evidence> list = new ArrayList<>();
            list.add(evidence);
            bifurcationEvidenceMap.put(packingAddress, list);
        }else {
            List<Evidence> evidenceList = bifurcationEvidenceMap.get(packingAddress);
            if(evidenceList.size() >= NulsContext.REDPUNISH_BIFURCATION){
                return;
            }
            ListIterator<Evidence> iterator = evidenceList.listIterator();
            boolean isSerialRoundIndex = false;
            while (iterator.hasNext()) {
                Evidence e = iterator.next();
                //如果与其中一个记录的轮次是连续的，则加入记录
                if (e.getRoundIndex() + 1 == extendsData.getRoundIndex()) {
                    iterator.add(evidence);
                    isSerialRoundIndex = true;
                }
            }
            if (!isSerialRoundIndex) {
                //分叉不是连续的轮次，则清空记录(重置).
                bifurcationEvidenceMap.remove(packingAddress);
            }
        }
        bifurcationEvidenceStorageService.save(Evidence.bifurcationEvidenceMapToPoMap(bifurcationEvidenceMap));
    }

    /**
     * 是否进行红牌惩罚
     * @param agent
     * @return
     */
    private boolean isRedPunish(Agent agent){
        String packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress());
        if(bifurcationEvidenceMap.containsKey(packingAddress)) {
            if (bifurcationEvidenceMap.get(packingAddress).size() >= NulsContext.REDPUNISH_BIFURCATION) {
                //分叉连续轮次没有达3轮就不进行红牌惩罚
                return true;
            }
        }
        return false;
    }

}