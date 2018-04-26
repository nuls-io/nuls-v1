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
import io.nuls.consensus.poc.protocol.event.notice.CancelConsensusNotice;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.consensus.poc.service.PocConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.UpdateDepositByAgentIdParam;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class StopAgentTxService implements TransactionService<StopAgentTransaction> {

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);


    @Override
    @DbSession
    public void onRollback(StopAgentTransaction tx, Block block) {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
        Consensus<Agent> ca = raTx.getTxData();
        ca.getExtend().setBlockHeight(raTx.getBlockHeight());
        ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        ca.getExtend().setTxHash(raTx.getHash().getDigestHex());
        AgentPo agentPo = ConsensusTool.agentToPojo(ca);
        agentPo.setBlockHeight(joinTx.getBlockHeight());
        agentPo.setDelHeight(0L);
        this.agentDataService.updateSelective(agentPo);

//            this.ledgerService.unlockTxRollback(tx.getTxData().getDigestHex());

        UpdateDepositByAgentIdParam dpo = new UpdateDepositByAgentIdParam();
        dpo.setAgentId(ca.getHexHash());
        dpo.setOldDelHeight(joinTx.getBlockHeight());
        dpo.setNewDelHeight(0L);
        this.depositDataService.updateSelectiveByAgentHash(dpo);

        //cache deposit
        Map<String, Object> params = new HashMap<>();
        params.put("agentHash", raTx.getTxData().getHexHash());
        List<DepositPo> polist = this.depositDataService.getList(params);
        if (null != polist) {
            for (DepositPo po : polist) {
                Consensus<Deposit> cd = ConsensusTool.fromPojo(po);
                this.ledgerService.unlockTxRollback(po.getTxHash());
            }
        }

//            CancelConsensusNotice notice = new CancelConsensusNotice();
//            notice.setEventBody(tx);
//            NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);


    }

    @Override
    @DbSession
    public void onCommit(StopAgentTransaction tx, Block block) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;

        List<Consensus<Deposit>> depositList = NulsContext.getServiceBean(PocConsensusService.class).getEffectiveDepositList(null, raTx.getTxData().getHexHash(), block.getHeader().getHeight(), null);
        for (Consensus<Deposit> depositConsensus : depositList) {
            if (!depositConsensus.getExtend().getAgentHash().equals(raTx.getTxData().getHexHash())) {
                continue;
            }
            if (depositConsensus.getExtend().getBlockHeight() > tx.getBlockHeight()) {
                continue;
            }
            if (0!=depositConsensus.getDelHeight()&&depositConsensus.getDelHeight() < tx.getBlockHeight()) {
                continue;
            }
            ledgerService.unlockTxSave(depositConsensus.getExtend().getTxHash());
        }

        this.agentDataService.deleteById(raTx.getTxData().getHexHash(), tx.getBlockHeight());


        DepositPo delPo = new DepositPo();
        delPo.setAgentHash(raTx.getTxData().getHexHash());
        delPo.setDelHeight(tx.getBlockHeight());
        this.depositDataService.deleteByAgentHash(delPo);

    }

    @Override
    public ValidateResult conflictDetect(StopAgentTransaction tx, List<Transaction> txList) {
        for (Transaction transaction : txList) {
            if (transaction.getHash().equals(tx.getHash())) {
                return ValidateResult.getFailedResult(ErrorCode.FAILED, "transaction Duplication");
            }
        }
        return ValidateResult.getSuccessResult();
    }

}
