package io.nuls.core.chain.entity;

/**
 * @author Niels
 * @date 2017/12/14
 */
public interface TransactionListener<T extends Transaction> {

    void onRollback(T tx);

    void onCommit(T tx);
}
