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

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 */
@Component
public class StopAgentTxProcessor implements TransactionProcessor<StopAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Override
    public Result onRollback(StopAgentTransaction tx, Object secondaryData) {
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() < 0) {
            throw new NulsRuntimeException(PocConsensusErrorCode.AGENT_NOT_EXIST);
        }
        agentPo.setDelHeight(-1L);
        List<DepositPo> depositPoList = depositStorageService.getList();
        for (DepositPo depositPo : depositPoList) {
            if (depositPo.getDelHeight() != tx.getBlockHeight()) {
                continue;
            }
            if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositPo.setDelHeight(-1L);
            depositStorageService.save(depositPo);
        }
        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            return Result.getFailed(PocConsensusErrorCode.UPDATE_AGENT_FAILED);
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(StopAgentTransaction tx, Object secondaryData) {
        BlockHeader header = (BlockHeader) secondaryData;
        if (tx.getTime() < (header.getTime() - 300000L)) {
            return Result.getFailed(PocConsensusErrorCode.LOCK_TIME_NOT_REACHED);
        }
        AgentPo agentPo = agentStorageService.get(tx.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            throw new NulsRuntimeException(PocConsensusErrorCode.AGENT_NOT_EXIST);
        }
        List<DepositPo> depositPoList = depositStorageService.getList();
        for (DepositPo depositPo : depositPoList) {
            if (depositPo.getDelHeight() > -1L) {
                continue;
            }
            if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositPo.setDelHeight(tx.getBlockHeight());
            depositStorageService.save(depositPo);
        }
        agentPo.setDelHeight(tx.getBlockHeight());
        tx.getTxData().setAddress(agentPo.getAgentAddress());

        boolean b = agentStorageService.save(agentPo);
        if (!b) {
            return Result.getFailed(PocConsensusErrorCode.UPDATE_AGENT_FAILED);
        }
        return Result.getSuccess();
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        if (txList == null || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        Set<NulsDigestData> hashSet = new HashSet<>();
        Set<String> addressSet = new HashSet<>();
        for (Transaction tx : txList) {
            if (tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH) {
                RedPunishTransaction transaction = (RedPunishTransaction) tx;
                addressSet.add(AddressTool.getStringAddressByBytes(transaction.getTxData().getAddress()));
            } else if (tx.getType() == ConsensusConstant.TX_TYPE_STOP_AGENT) {
                StopAgentTransaction transaction = (StopAgentTransaction) tx;
                if (!hashSet.add(transaction.getTxData().getCreateTxHash())) {
                    ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TRANSACTION_REPEATED);
                    result.setData(transaction);
                    return result;
                }
                if (transaction.getTxData().getAddress() == null) {
                    CreateAgentTransaction agentTransaction = (CreateAgentTransaction) ledgerService.getTx(transaction.getTxData().getCreateTxHash());
                    if (null == agentTransaction) {
                        ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.AGENT_NOT_EXIST);
                        result.setData(transaction);
                        return result;
                    }
                    transaction.getTxData().setAddress(agentTransaction.getTxData().getAgentAddress());
                }
                if (addressSet.contains(AddressTool.getStringAddressByBytes(transaction.getTxData().getAddress()))) {
                    ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.AGENT_STOPPED);
                    result.setData(transaction);
                    return result;
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }
}
