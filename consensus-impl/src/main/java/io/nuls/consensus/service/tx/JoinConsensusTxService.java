package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.DelegatePo;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction> {
    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private DelegateDataService delegateDataService = NulsContext.getInstance().getService(DelegateDataService.class);

    @Override
    public void onRollback(PocJoinConsensusTransaction tx) throws NulsException {
        this.manager.delDelegate(tx.getTxData().getExtend().getHash());
        delegateDataService.delete(tx.getTxData().getExtend().getHash());
    }

    @Override
    public void onCommit(PocJoinConsensusTransaction tx) throws NulsException {
        manager.changeDelegateStatus(tx.getTxData().getExtend().getHash(), ConsensusStatusEnum.IN);
        DelegatePo po = new DelegatePo();
        po.setId(tx.getTxData().getExtend().getHash());
        po.setStatus(ConsensusStatusEnum.IN.getCode());
        delegateDataService.updateSelective(po);
    }

    @Override
    public void onApproval(PocJoinConsensusTransaction tx) throws NulsException {
        Consensus<Delegate> cd = tx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        manager.cacheDelegate(cd);
        DelegatePo po = ConsensusTool.delegateToPojo(cd);
        delegateDataService.save(po);
    }
}
