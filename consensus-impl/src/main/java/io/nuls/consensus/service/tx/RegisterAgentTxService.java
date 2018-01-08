package io.nuls.consensus.service.tx;

import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class RegisterAgentTxService implements TransactionService<RegisterAgentTransaction>{
    @Override
    public void onRollback(RegisterAgentTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onCommit(RegisterAgentTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void onApproval(RegisterAgentTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}
