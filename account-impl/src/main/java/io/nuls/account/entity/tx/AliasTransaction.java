package io.nuls.account.entity.tx;


import io.nuls.account.entity.Alias;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasTransaction extends AbstractCoinTransaction<Alias> {

    public AliasTransaction() throws NulsException {
        super(TransactionConstant.TX_TYPE_SET_ALIAS, null, null);
    }

    public AliasTransaction(CoinTransferData coinParam, String password) throws NulsException {
        super(TransactionConstant.TX_TYPE_SET_ALIAS, coinParam, password);
    }

    @Override
    protected Alias parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return new Alias(byteBuffer);
    }
}
