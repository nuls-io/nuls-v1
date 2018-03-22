/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.notice.CancelConsensusNotice;
import io.nuls.consensus.event.notice.StopConsensusNotice;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DelegatePo;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class ExitConsensusTxService implements TransactionService<PocExitConsensusTransaction> {

    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private AgentDataService delegateAccountService = NulsContext.getServiceBean(AgentDataService.class);
    private DelegateDataService delegateDataService = NulsContext.getServiceBean(DelegateDataService.class);

    @Override
    public void onRollback(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            Consensus<Agent> ca = raTx.getTxData();
            ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
            manager.cacheAgent(ca);
            AgentPo agentPo = new AgentPo();
            agentPo.setAgentAddress(raTx.getTxData().getAddress());
            agentPo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.delegateAccountService.updateSelective(agentPo);
            DelegatePo dpo = new DelegatePo();
            dpo.setAgentAddress(raTx.getTxData().getAddress());
            dpo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.delegateDataService.updateSelectiveByAgentAddress(dpo);
            CancelConsensusNotice notice = new CancelConsensusNotice();
            notice.setEventBody(tx);
            NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
            //cache delegates
            Map<String, Object> params = new HashMap<>();
            params.put("agentAddress", raTx.getTxData().getAddress());
            List<DelegatePo> polist = this.delegateDataService.getList(params);
            if (null == polist || polist.isEmpty()) {
                return;
            }
            for (DelegatePo po : polist) {
                Consensus<Delegate> cd = ConsensusTool.fromPojo(po);
                this.manager.cacheDelegate(cd);
            }
            //todo 重新锁定所有解锁的金额
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Delegate> cd = pjcTx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
        manager.cacheDelegate(cd);
        DelegatePo dPo = this.delegateDataService.get(cd.getExtend().getHash());
        if (dPo == null) {
            dPo = ConsensusTool.delegateToPojo(cd);
            this.delegateDataService.save(dPo);
        }
        StopConsensusNotice notice = new StopConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
        //todo 重新锁定解锁的金额
    }

    @Override
    public void onCommit(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.delAgent(raTx.getTxData().getAddress());
            manager.delDelegateByAgent(raTx.getTxData().getAddress());
            AgentPo agentPo = new AgentPo();
            agentPo.setAgentAddress(raTx.getTxData().getAddress());
            agentPo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
            this.delegateAccountService.updateSelective(agentPo);
            DelegatePo dpo = new DelegatePo();
            dpo.setAgentAddress(raTx.getTxData().getAddress());
            dpo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
            this.delegateDataService.updateSelectiveByAgentAddress(dpo);
            //todo 彻底解锁抵押的金额
            //todo 解锁所有参与委托的金额
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Delegate> cd = pjcTx.getTxData();
        manager.delDelegate(cd.getExtend().getHash());
        DelegatePo dpo = new DelegatePo();
        dpo.setId(cd.getExtend().getHash());
        dpo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        this.delegateDataService.updateSelective(dpo);
        //todo 彻底解锁抵押的金额
    }

    @Override
    public void onApproval(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.changeAgentStatus(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            manager.changeDelegateStatusByAgent(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            //todo 解锁抵押的金额
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Delegate> cd = pjcTx.getTxData();
        manager.changeDelegateStatus(cd.getExtend().getHash(), ConsensusStatusEnum.NOT_IN);
        //todo 内存解锁抵押的金额
    }
}
