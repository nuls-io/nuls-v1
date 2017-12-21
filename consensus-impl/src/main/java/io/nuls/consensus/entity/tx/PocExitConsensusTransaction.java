package io.nuls.consensus.entity.tx;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class PocExitConsensusTransaction extends UnlockNulsTransaction<NulsDigestData> {

    public PocExitConsensusTransaction() {
        super(TransactionConstant.TX_TYPE_EXIT_CONSENSUS);
    }

    @Override
    protected NulsDigestData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        NulsDigestData data = new NulsDigestData();
        data.parse(byteBuffer);
        return data;
    }
}
