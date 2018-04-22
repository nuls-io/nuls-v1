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
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.event.notice.EntrustConsensusNotice;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.UpdateDepositByAgentIdParam;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction> {
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @Override
    @DbSession
    public void onRollback(PocJoinConsensusTransaction tx, Block block) throws NulsException {

        DepositPo delPo = new DepositPo();
        delPo.setId(tx.getTxData().getHexHash());
        delPo.setDelHeight(tx.getBlockHeight());
        depositDataService.realDeleteById(delPo);

//        Consensus<Deposit> cd = tx.getTxData();
//
//        List<DepositPo> poList = depositDataService.getEffectiveList(null, block.getHeader().getHeight(),cd.getHexHash(), null);
//        long total = 0L;
//        for (DepositPo dpo : poList) {
//            total += dpo.getDeposit();
//            dpo.setStatus(ConsensusStatusEnum.WAITING.getCode());
//        }
//        if (total < PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT.getValue()) {
//
//            AgentPo agentPo = new AgentPo();
//            agentPo.setId(cd.getExtend().getAgentHash());
//            agentPo.setStatus(ConsensusStatusEnum.WAITING.getCode());
//            this.agentDataService.updateSelective(agentPo);
//
//            depositDataService.update(poList);
//        }
    }

    @Override
    @DbSession
    public void onCommit(PocJoinConsensusTransaction tx, Block block) throws NulsException {
        Consensus<Deposit> cd = tx.getTxData();
        cd.getExtend().setBlockHeight(tx.getBlockHeight());
        cd.getExtend().setTxHash(tx.getHash().getDigestHex());
        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());

        DepositPo po = ConsensusTool.depositToPojo(cd, tx.getHash().getDigestHex());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTime(tx.getTime());
        depositDataService.save(po);


//        List<DepositPo> poList = depositDataService.getEffectiveList(null, block.getHeader().getHeight(), po.getAgentHash(), null);
//        long total = 0L;
//        for (DepositPo dpo : poList) {
//            total += dpo.getDeposit();
//            dpo.setStatus(ConsensusStatusEnum.IN.getCode());
//        }
//        if (total >= PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT.getValue()) {
//
//            AgentPo agentPo = new AgentPo();
//            agentPo.setId(cd.getExtend().getAgentHash());
//            agentPo.setStatus(ConsensusStatusEnum.IN.getCode());
//            this.agentDataService.updateSelective(agentPo);
//
//            depositDataService.update(poList);
//        }


        EntrustConsensusNotice notice = new EntrustConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }

    @Override
    public ValidateResult conflictDetect(PocJoinConsensusTransaction tx, List<Transaction> txList) {
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        AgentPo agent = agentDataService.get(tx.getTxData().getExtend().getAgentHash());
        if (null == agent) {
            return ValidateResult.getFailedResult("the agent is not exist!");
        }
        RegisterAgentTransaction registerAgentTransaction = (RegisterAgentTransaction) this.ledgerService.getTx(NulsDigestData.fromDigestHex(agent.getTxHash()));
        if (null == registerAgentTransaction) {
            return ValidateResult.getFailedResult(ErrorCode.DATA_ERROR, "the agent's txHash is wrong!");
        }
        Long value = 0L;
        for (Transaction transaction : txList) {
            if (transaction.getHash().equals(tx.getHash())) {
                return ValidateResult.getFailedResult(ErrorCode.FAILED, "transaction Duplication");
            }
            switch (transaction.getType()) {
                case TransactionConstant.TX_TYPE_STOP_AGENT:
                    StopAgentTransaction stopAgentTransaction = (StopAgentTransaction) transaction;
                    if (stopAgentTransaction.getHash().getDigestHex().equals(agent.getTxHash())) {
                        return ValidateResult.getFailedResult(ErrorCode.FAILED, "the agent has been stoped!");
                    }
                    break;
                case TransactionConstant.TX_TYPE_JOIN_CONSENSUS:
                    PocJoinConsensusTransaction pocJoinConsensusTransaction = (PocJoinConsensusTransaction) transaction;
                    value += pocJoinConsensusTransaction.getTxData().getExtend().getDeposit().getValue();
                    break;
                case TransactionConstant.TX_TYPE_RED_PUNISH:
                    RedPunishTransaction redPunishTransaction = (RedPunishTransaction) transaction;
                    if (redPunishTransaction.getTxData().getAddress().equals(agent.getAgentAddress())) {
                        return ValidateResult.getFailedResult(ErrorCode.LACK_OF_CREDIT, "there is a new Red Punish Transaction of the agent address!");
                    }
                    break;
            }
        }
        if (value > 0L) {
            List<DepositPo> list = this.depositDataService.getEffectiveList(null, NulsContext.getInstance().getBestHeight(), agent.getId(), null);
            Long allready = 0L;
            for (DepositPo po : list) {
                allready += po.getDeposit();
            }
            if ((allready + value + tx.getTxData().getExtend().getDeposit().getValue()) > PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT.getValue()) {
                return ValidateResult.getFailedResult("there is too much deposit of the agent!");
            }
        }
        return ValidateResult.getSuccessResult();
    }


}
