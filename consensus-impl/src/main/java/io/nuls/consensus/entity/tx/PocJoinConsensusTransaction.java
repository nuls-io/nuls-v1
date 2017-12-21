package io.nuls.consensus.entity.tx;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.constant.TransactionConstant;
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

    public PocJoinConsensusTransaction(CoinTransferData lockData, String password) {
        super(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, lockData, password);
    }

    @Override
    protected Consensus<Delegate> parseTxData(NulsByteBuffer byteBuffer) {
        Consensus<Delegate> con = new Consensus<Delegate>();
        con.parse(byteBuffer);
        Delegate agent = new Delegate();
        agent.parse(byteBuffer);
        con.setExtend(agent);
        return con;
    }
}
