package io.nuls.consensus.service.tx;

import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction>{
    @Override
    public void onRollback(PocJoinConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(PocJoinConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(PocJoinConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}
