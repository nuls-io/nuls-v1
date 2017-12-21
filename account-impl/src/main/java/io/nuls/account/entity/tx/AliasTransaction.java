package io.nuls.account.entity.tx;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.Alias;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasTransaction extends AbstractCoinTransaction<Alias> {



    public AliasTransaction(CoinTransferData coinParam, String password) {
        super(TransactionConstant.TX_TYPE_SET_ALIAS, coinParam, password);
    }

    @Override
    protected Alias parseTxData(NulsByteBuffer byteBuffer) {


        return null;
    }
}
