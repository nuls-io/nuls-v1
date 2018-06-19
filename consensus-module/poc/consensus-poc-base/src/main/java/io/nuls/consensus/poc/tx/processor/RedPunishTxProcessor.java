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
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class RedPunishTxProcessor implements TransactionProcessor<RedPunishTransaction> {

    @Autowired
    private PunishLogStorageService storageService;

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Override
    public Result onRollback(RedPunishTransaction tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();

        List<AgentPo> agentList = agentStorageService.getList();
        AgentPo agent = null;
        for (AgentPo agent_ : agentList) {
            if (agent_.getDelHeight() <= 0) {
                continue;
            }
            if (Arrays.equals(agent_.getAgentAddress(), punishData.getAddress())) {
                agent = agent_;
                break;
            }
        }
        if (null == agent) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "There is no agent can be punished.");
        }
        List<DepositPo> depositPoList = depositStorageService.getList();
        List<DepositPo> updatedList = new ArrayList<>();
        for(DepositPo po:depositPoList){
            po.setDelHeight(-1);
            boolean success = this.depositStorageService.save(po);
            if(!success){
                for(DepositPo po2:depositPoList) {
                    po2.setDelHeight(tx.getBlockHeight());
                    this.depositStorageService.save(po2);
                }
                return Result.getFailed(KernelErrorCode.DATA_ERROR, "Update deposit failed!");
            }
            updatedList.add(po);
        }
        AgentPo agentPo = agent;
        agentPo.setDelHeight(-1L);
        boolean success = agentStorageService.save(agentPo);
        if (!success) {
            for(DepositPo po2:depositPoList) {
                po2.setDelHeight(tx.getBlockHeight());
                this.depositStorageService.save(po2);
            }
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't update the agent!");
        }
        success = storageService.delete(getPoKey(punishData.getAddress(), PunishType.RED.getCode(), tx.getBlockHeight()));
        if (!success) {
            for(DepositPo po2:depositPoList) {
                po2.setDelHeight(tx.getBlockHeight());
                this.depositStorageService.save(po2);
            }
            agentPo.setDelHeight(tx.getBlockHeight());
            agentStorageService.save(agentPo);
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollbackTransaction tx failed!");
        }

        return Result.getSuccess();
    }


    @Override
    public Result onCommit(RedPunishTransaction tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        BlockRoundData roundData = new BlockRoundData(header.getExtend());
        PunishLogPo punishLogPo = new PunishLogPo();
        punishLogPo.setAddress(punishData.getAddress());
        punishLogPo.setHeight(tx.getBlockHeight());
        punishLogPo.setRoundIndex(roundData.getRoundIndex());
        punishLogPo.setTime(tx.getTime());
        punishLogPo.setType(PunishType.RED.getCode());

        List<AgentPo> agentList = agentStorageService.getList();
        AgentPo agent = null;
        for (AgentPo agent_ : agentList) {
            if (agent_.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(agent_.getAgentAddress(), punishLogPo.getAddress())) {
                agent = agent_;
                break;
            }
        }
        if (null == agent) {
            Log.error("There is no agent can be punished.");
            return Result.getSuccess();
        }


        List<DepositPo> depositPoList = depositStorageService.getList();
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (!po.getAgentHash().equals(agent.getHash())) {
                continue;
            }
            po.setDelHeight(tx.getBlockHeight());
            boolean b = depositStorageService.save(po);
            if (!b) {
                for(DepositPo po2:updatedList){
                    po2.setDelHeight(-1);
                    this.depositStorageService.save(po2);
                }
                return ValidateResult.getFailedResult(this.getClass().getName(), "update deposit failed!");
            }
            updatedList.add(po);
        }
        boolean success = storageService.save(punishLogPo);
        if (!success) {
            for(DepositPo po2:updatedList){
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2);
            }
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollbackTransaction tx failed!");
        }
        AgentPo agentPo = agent;
        agentPo.setDelHeight(tx.getBlockHeight());
        success = agentStorageService.save(agentPo);
        if (!success) {
            for(DepositPo po2:updatedList){
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2);
            }
            this.storageService.delete(punishLogPo.getKey());
            return Result.getFailed(KernelErrorCode.DATA_ERROR, "Can't update the agent!");
        }
        return Result.getSuccess();
    }

    /**
     * 获取固定格式的key
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight) {
        return ArraysTool.joinintTogether(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight));
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }
}
