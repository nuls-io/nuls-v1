package io.nuls.consensus.entity.tx;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class PocJoinConsensusTransaction extends LockNulsTransaction<Consensus<Delegate>> {
    public PocJoinConsensusTransaction() {
        super(TransactionConstant.TX_TYPE_JOIN_CONSENSUS);
    }

    public PocJoinConsensusTransaction(CoinTransferData lockData, String password) throws NulsException {
        super(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, lockData, password);
    }

    @Override
    protected Consensus<Delegate> parseTxData(NulsByteBuffer byteBuffer) throws NulsException{
        Consensus<Delegate> consensus = byteBuffer.readNulsData(new Consensus<>());
        Delegate delegate = byteBuffer.readNulsData(new Delegate());
        consensus.setExtend(delegate);
        return consensus;
    }
}
