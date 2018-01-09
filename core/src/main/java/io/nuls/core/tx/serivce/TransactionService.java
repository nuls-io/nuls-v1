package io.nuls.core.tx.serivce;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;

/**
 * @author Niels
 * @date 2017/12/14
 */
public interface TransactionService<T extends Transaction> {

    void onRollback(T tx) throws NulsException;

    void onCommit(T tx) throws NulsException;

    void onApproval(T tx) throws NulsException;
}
