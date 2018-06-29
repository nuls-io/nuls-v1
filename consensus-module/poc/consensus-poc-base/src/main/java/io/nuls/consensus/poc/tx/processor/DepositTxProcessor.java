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

package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;

import java.util.*;

/**
 * @author: Niels Wang
 */
@Component
public class DepositTxProcessor implements TransactionProcessor<DepositTransaction> {

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private AgentStorageService agentStorageService;

    /**
     * 交易回滚时调用该方法
     * This method is called when the transaction rolls back.
     *
     * @param tx            要回滚的交易，The transaction to roll back.
     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
     */
    @Override
    public Result onRollback(DepositTransaction tx, Object secondaryData) {
        Deposit deposit = tx.getTxData();
        deposit.setTxHash(tx.getHash());

        boolean success = depositStorageService.delete(deposit.getTxHash());
        return new Result(success, null);
    }

    /**
     * 交易存储时调用该方法
     * This method is called when the transaction save.
     *
     * @param tx            要保存的交易，The transaction to save;
     * @param secondaryData 辅助数据，视业务需要传递，Secondary data, depending on the business needs to be passed.
     */
    @Override
    public Result onCommit(DepositTransaction tx, Object secondaryData) {
        Deposit deposit = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        deposit.setTxHash(tx.getHash());
        deposit.setTime(tx.getTime());
        deposit.setBlockHeight(header.getHeight());

        DepositPo depositPo = PoConvertUtil.depositToPo(deposit);

        boolean success = depositStorageService.save(depositPo);
        return new Result(success, null);
    }

    /**
     * 冲突检测，检测如果传入的交易列表中有相冲突的交易，则返回失败，写明失败原因及所有的应该舍弃的交易列表
     * 本方法不检查双花冲突，双花由账本接口实现
     * <p>
     * Conflict detection, which detects conflicting transactions in the incoming transaction list, returns failure,
     * indicating the cause of failure and all the list of trades that should be discarded.
     * This method does not check the double flower conflict, the double flower is realized by the accounting interface.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     * @return 操作结果：成功则返回successResult，失败时，data中返回丢弃列表，msg中返回冲突原因
     * Operation result: success returns successResult. When failure, data returns the discard list, and MSG returns the cause of conflict.
     */
    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }

        Set<NulsDigestData> outAgentHash = new HashSet<>();
        Map<NulsDigestData, Na> naMap = new HashMap<>();
        List<DepositTransaction> dTxList = new ArrayList<>();
        for (Transaction transaction : txList) {
            switch (transaction.getType()) {
                case ConsensusConstant.TX_TYPE_STOP_AGENT:
                    StopAgentTransaction stopAgentTransaction = (StopAgentTransaction) transaction;
                    outAgentHash.add(stopAgentTransaction.getTxData().getCreateTxHash());
                    break;
                case ConsensusConstant.TX_TYPE_JOIN_CONSENSUS:
                    DepositTransaction depositTransaction = (DepositTransaction) transaction;
                    Na na = naMap.get(depositTransaction.getTxData().getAgentHash());
                    if (null == na) {
                        na = getAgentTotalDeposit(depositTransaction.getTxData().getAgentHash());
                    }
                    if (na == null) {
                        na = depositTransaction.getTxData().getDeposit();
                    } else {
                        na = na.add(depositTransaction.getTxData().getDeposit());
                    }
                    if (na.isGreaterThan(PocConsensusProtocolConstant.SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT)) {
                        ValidateResult validateResult = ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.DEPOSIT_TOO_MUCH);
                        validateResult.setData(transaction);
                        return validateResult;
                    } else {
                        naMap.put(depositTransaction.getTxData().getAgentHash(), na);
                    }
                    dTxList.add(depositTransaction);
                    break;
                case ConsensusConstant.TX_TYPE_RED_PUNISH:
                    RedPunishTransaction redPunishTransaction = (RedPunishTransaction) transaction;
                    RedPunishData redPunishData = redPunishTransaction.getTxData();
                    AgentPo agent = this.getAgentByAddress(redPunishData.getAddress());
                    if (null != agent) {
                        outAgentHash.add(agent.getHash());
                    }
                    break;
            }
        }

        if (dTxList.isEmpty() || outAgentHash.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        for (DepositTransaction depositTransaction : dTxList) {
            if (outAgentHash.contains(depositTransaction.getTxData().getAgentHash())) {
                ValidateResult validateResult = ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.AGENT_STOPPED);
                validateResult.setData(depositTransaction);
                return validateResult;
            }
        }
        return ValidateResult.getSuccessResult();
    }

    private AgentPo getAgentByAddress(byte[] address) {
        List<AgentPo> agentList = agentStorageService.getList();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        for (AgentPo agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return agent;
            }
        }
        return null;
    }

    private Na getAgentTotalDeposit(NulsDigestData hash) {
        List<DepositPo> depositList = depositStorageService.getList();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        Na na = Na.ZERO;
        for (DepositPo deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!deposit.getAgentHash().equals(hash)) {
                continue;
            }
            na = na.add(deposit.getDeposit());
        }
        return na;
    }
}
