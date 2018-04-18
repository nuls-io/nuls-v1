package io.nuls.ledger.entity.tx;

import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Transaction;

/**
 * author Facjas
 * date 2018/4/3.
 */
public abstract class AbstractNoneCoinTransaction <T extends BaseNulsData> extends Transaction<T> {
    public AbstractNoneCoinTransaction(int type) {
        super(type);
    }
}
