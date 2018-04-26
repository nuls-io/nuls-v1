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
package io.nuls.consensus.poc.service.impl.tx;

import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.event.notice.RegisterAgentNotice;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class RegisterAgentTxService implements TransactionService<RegisterAgentTransaction> {
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);

    @Override
    @DbSession
    public void onRollback(RegisterAgentTransaction tx, Block block) {
//        this.agentDataService.deleteById(tx.getTxData().getHexHash(), tx.getBlockHeight());

        agentDataService.realDeleteById(tx.getTxData().getHexHash(), 0);

        DepositPo delPo = new DepositPo();
        delPo.setAgentHash(tx.getTxData().getHexHash());
        delPo.setDelHeight(tx.getBlockHeight());
        this.depositDataService.realDeleteByAgentHash(delPo);
    }

    @Override
    @DbSession
    public void onCommit(RegisterAgentTransaction tx, Block block) {
        Consensus<Agent> ca = tx.getTxData();
        ca.getExtend().setBlockHeight(tx.getBlockHeight());
        ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        ca.getExtend().setTxHash(tx.getHash().getDigestHex());
        AgentPo po = ConsensusTool.agentToPojo(ca);
        agentDataService.save(po);

        RegisterAgentNotice notice = new RegisterAgentNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }

    @Override
    public ValidateResult conflictDetect(RegisterAgentTransaction tx, List<Transaction> txList) {
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        for (Transaction transaction : txList) {
            if(transaction.getHash().equals(tx.getHash())){
                return ValidateResult.getFailedResult(ErrorCode.FAILED,"transaction Duplication");
            }
            switch (transaction.getType()) {
                case TransactionConstant.TX_TYPE_REGISTER_AGENT:
                    RegisterAgentTransaction registerAgentTransaction = (RegisterAgentTransaction) transaction;
                    if(registerAgentTransaction.getTxData().getAddress().equals(tx.getTxData().getAddress())||
                            registerAgentTransaction.getTxData().getAddress().equals(tx.getTxData().getExtend().getPackingAddress())||
                            registerAgentTransaction.getTxData().getExtend().getPackingAddress().equals(tx.getTxData().getAddress())
                            ||registerAgentTransaction.getTxData().getExtend().getPackingAddress().equals(tx.getTxData().getExtend().getPackingAddress())){
                        return ValidateResult.getFailedResult(ErrorCode.FAILED, "there is a agent has same address!");
                    }
                    break;
                case TransactionConstant.TX_TYPE_RED_PUNISH:
                    RedPunishTransaction redPunishTransaction = (RedPunishTransaction) transaction;
                    if (redPunishTransaction.getTxData().getAddress().equals(tx.getTxData().getAddress())) {
                        return ValidateResult.getFailedResult(ErrorCode.LACK_OF_CREDIT, "there is a new Red Punish Transaction!");
                    }
                    break;
            }
        }

        return ValidateResult.getSuccessResult();
    }
}

