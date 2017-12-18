package io.nuls.consensus.entity.tx;

import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class PocJoinConsensusTransaction extends JoinConsensusTransaction {
    private LockNulsTransaction lockNulsTransaction;

    public PocJoinConsensusTransaction() {
        super();
    }

    public LockNulsTransaction getLockNulsTransaction() {
        return lockNulsTransaction;
    }

    public void setLockNulsTransaction(LockNulsTransaction lockNulsTransaction) {
        this.lockNulsTransaction = lockNulsTransaction;
    }

}
