package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;
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

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private DelegateAccountDataService delegateAccountService = NulsContext.getInstance().getService(DelegateAccountDataService.class);
    private DelegateDataService delegateDataService = NulsContext.getInstance().getService(DelegateDataService.class);

    @Override
    public void onRollback(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            Consensus<Agent> ca = raTx.getTxData();
            ca.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
            manager.cacheAgent(ca);
            DelegateAccountPo agentPo = new DelegateAccountPo();
            agentPo.setId(raTx.getTxData().getAddress());
            agentPo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.delegateAccountService.updateSelective(agentPo);
            DelegatePo dpo = new DelegatePo();
            dpo.setAgentAddress(raTx.getTxData().getAddress());
            dpo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.delegateDataService.updateSelectiveByAgentAddress(dpo);
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
    }

    @Override
    public void onCommit(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.delAgent(raTx.getTxData().getAddress());
            manager.delDelegateByAgent(raTx.getTxData().getAddress());
            DelegateAccountPo agentPo = new DelegateAccountPo();
            agentPo.setId(raTx.getTxData().getAddress());
            agentPo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
            this.delegateAccountService.updateSelective(agentPo);
            DelegatePo dpo = new DelegatePo();
            dpo.setAgentAddress(raTx.getTxData().getAddress());
            dpo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
            this.delegateDataService.updateSelectiveByAgentAddress(dpo);
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Delegate> cd = pjcTx.getTxData();
        manager.delDelegate(cd.getExtend().getHash());
        DelegatePo dpo = new DelegatePo();
        dpo.setId(cd.getExtend().getHash());
        dpo.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        this.delegateDataService.updateSelective(dpo);
    }

    @Override
    public void onApproval(PocExitConsensusTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            RegisterAgentTransaction raTx = (RegisterAgentTransaction) joinTx;
            manager.changeAgentStatus(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            manager.changeDelegateStatusByAgent(raTx.getTxData().getAddress(), ConsensusStatusEnum.NOT_IN);
            return;
        }
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Delegate> cd = pjcTx.getTxData();
        manager.changeDelegateStatus(cd.getExtend().getHash(), ConsensusStatusEnum.NOT_IN);
    }
}
