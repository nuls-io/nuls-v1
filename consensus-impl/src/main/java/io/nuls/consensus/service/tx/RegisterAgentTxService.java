package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class RegisterAgentTxService implements TransactionService<RegisterAgentTransaction> {
    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private DelegateAccountDataService delegateAccountService = NulsContext.getInstance().getService(DelegateAccountDataService.class);

    @Override
    public void onRollback(RegisterAgentTransaction tx) throws NulsException {
        this.manager.delAgent(tx.getTxData().getAddress());
        this.delegateAccountService.delete(tx.getTxData().getAddress());
    }

    @Override
    public void onCommit(RegisterAgentTransaction tx) throws NulsException {
        manager.changeAgentStatus(tx.getTxData().getAddress(), ConsensusStatusEnum.IN);
        DelegateAccountPo po = new DelegateAccountPo();
        po.setId(tx.getTxData().getAddress());
        po.setStatus(ConsensusStatusEnum.IN.getCode());
        delegateAccountService.updateSelective(po);
    }

    @Override
    public void onApproval(RegisterAgentTransaction tx) throws NulsException {
        Consensus<Agent> ca = tx.getTxData();
        ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        manager.cacheAgent(ca);
        DelegateAccountPo po = ConsensusTool.agentToPojo(ca);
        delegateAccountService.save(po);
    }
}
