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
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.PocJoinConsensusTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.TxAccountRelationDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.TxAccountRelationPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class CancelDepositTxService implements TransactionService<CancelDepositTransaction> {

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

//    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();

    private TxAccountRelationDataService relationDataService = NulsContext.getServiceBean(TxAccountRelationDataService.class);


    @Override
    @DbSession
    public void onRollback(CancelDepositTransaction tx, Block block) {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
        DepositPo dpo1 = new DepositPo();
        dpo1.setId(cd.getHexHash());
        dpo1.setDelHeight(0L);
        this.depositDataService.updateSelective(dpo1);
//        StopConsensusNotice notice = new StopConsensusNotice();
//        notice.setEventBody(tx);
//        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
        this.ledgerService.unlockTxRollback(tx.getTxData().getDigestHex());

//        Consensus<Deposit> depositConsensus = manager.getDepositById(cd.getHexHash());
//        depositConsensus.setDelHeight(0L);
//        manager.putDeposit(depositConsensus);

        Set<String> set = new HashSet<>();
        set.add(cd.getAddress());
        relationDataService.deleteRelation(tx.getHash().getDigestHex(), set);

//        List<DepositPo> poList = depositDataService.getEffectiveList(null, block.getHeader().getHeight(),cd.getExtend().getAgentHash(), null);
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


    }

    @Override
    @DbSession
    public void onCommit(CancelDepositTransaction tx, Block block) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        DepositPo dpo = new DepositPo();
        dpo.setDelHeight(tx.getBlockHeight());
        dpo.setId(cd.getHexHash());
        this.depositDataService.deleteById(dpo);
        this.ledgerService.unlockTxSave(tx.getTxData().getDigestHex());
//        manager.delDeposit(pjcTx.getTxData().getHexHash(), tx.getBlockHeight());

        TxAccountRelationPo po = new TxAccountRelationPo();
        po.setAddress(pjcTx.getTxData().getAddress());
        po.setTxHash(tx.getHash().getDigestHex());
        relationDataService.save(po);


//        List<DepositPo> poList = depositDataService.getEffectiveList(null, block.getHeader().getHeight(),cd.getHexHash(), null);
//        long total = 0L;
//        for (DepositPo dpo1 : poList) {
//            total += dpo1.getDeposit();
//            dpo1.setStatus(ConsensusStatusEnum.WAITING.getCode());
//        }
//        if (total < PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT.getValue()) {
//
//            AgentPo agentPo = new AgentPo();
//            agentPo.setStatus(ConsensusStatusEnum.WAITING.getCode());
//            agentPo.setId(cd.getExtend().getAgentHash());
//            this.agentDataService.updateSelective(agentPo);
//
//            depositDataService.update(poList);
//        }
    }

    @Override
    public ValidateResult conflictDetect(CancelDepositTransaction tx, List<Transaction> txList) {
        for (Transaction transaction : txList) {
            if (transaction.getHash().equals(tx.getHash())) {
                return ValidateResult.getFailedResult(ErrorCode.FAILED, "transaction Duplication");
            }

            if(tx.getTxData().equals(transaction.getTxData())){
                return ValidateResult.getFailedResult(ErrorCode.FAILED, "Cancel deposit transaction Duplication");
            }

        }
        return ValidateResult.getSuccessResult();
    }

}
