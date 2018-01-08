package io.nuls.consensus.service.tx;

import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class ExitConsensusTxService implements TransactionService<PocExitConsensusTransaction>{
    @Override
    public void onRollback(PocExitConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(PocExitConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(PocExitConsensusTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}
