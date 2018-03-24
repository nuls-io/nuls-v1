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
import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
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
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
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
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);

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
            this.agentDataService.updateSelective(agentPo);
            DepositPo dpo = new DepositPo();
            dpo.setAgentAddress(raTx.getTxData().getAddress());
            dpo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.depositDataService.updateSelectiveByAgentAddress(dpo);
            CancelConsensusNotice notice = new CancelConsensusNotice();
            notice.setEventBody(tx);
            NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
            //cache delegates
            Map<String, Object> params = new HashMap<>();
            params.put("agentAddress", raTx.getTxData().getAddress());
            List<DepositPo> polist = this.depositDataService.getList(params);
            if (null == polist || polist.isEmpty()) {
                return;
            }
            for (DepositPo po : polist) {
                Consensus<Deposit> cd = ConsensusTool.fromPojo(po);
                this.manager.cacheDeposit(cd);
            }
            this.ledgerService.unlockTxRollback(tx.getTxData().getDigestHex());
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("agentAddress", ca.getAddress());
            List<DepositPo> poList = depositDataService.getList(paramsMap);
            for(DepositPo po:poList){
                this.ledgerService.unlockTxRollback(po.getTxHash());
            }
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
        manager.cacheDeposit(cd);
        DepositPo dPo = this.depositDataService.get(cd.getHexHash());
        if (dPo == null) {
            dPo = ConsensusTool.depositToPojo(cd,tx.getHash().getDigestHex());
            this.depositDataService.save(dPo);
        }
        StopConsensusNotice notice = new StopConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
        this.ledgerService.unlockTxRollback(tx.getTxData().getDigestHex());
    }

    @Override
    public void onCommit(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.delAgent(raTx.getTxData().getAddress());
            manager.delDepositByAgent(raTx.getTxData().getAddress());

            this.ledgerService.unlockTxSave(tx.getTxData().getDigestHex());
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("agentAddress", raTx.getTxData().getAddress());
            List<DepositPo> poList = depositDataService.getList(paramsMap);
            for(DepositPo po:poList){
                this.ledgerService.unlockTxSave(po.getTxHash());
            }
            this.agentDataService.delete(raTx.getTxData().getAddress());
            this.depositDataService.deleteByAgentAddress(raTx.getTxData().getAddress());
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        manager.delDeposit(cd.getHexHash());
        this.depositDataService.delete(cd.getHexHash());
        this.ledgerService.unlockTxSave(tx.getTxData().getDigestHex());
    }

    @Override
    public void onApproval(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if(joinTx==null){
            joinTx = ConfirmingTxCacheManager.getInstance().getTx(tx.getTxData());
        }
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.changeAgentStatus(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            manager.changeDepositStatusByAgent(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            this.ledgerService.unlockTxApprove(tx.getTxData().getDigestHex());
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        manager.changeDepositStatus(cd.getHexHash(), ConsensusStatusEnum.NOT_IN);
        this.ledgerService.unlockTxApprove(tx.getTxData().getDigestHex());
    }
}
