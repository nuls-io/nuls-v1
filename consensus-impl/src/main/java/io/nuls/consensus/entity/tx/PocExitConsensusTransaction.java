package io.nuls.consensus.entity.tx;

import io.nuls.consensus.tx.ExitConsensusTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class PocExitConsensusTransaction extends ExitConsensusTransaction {
    private UnlockNulsTransaction unlockNulsTransaction;

    public PocExitConsensusTransaction() {
        super();
    }

    public UnlockNulsTransaction getUnlockNulsTransaction() {
        return unlockNulsTransaction;
    }

    public void setUnlockNulsTransaction(UnlockNulsTransaction unlockNulsTransaction) {
        this.unlockNulsTransaction = unlockNulsTransaction;
    }
}
