package io.nuls.protocol.storage.service;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;

public interface TransactionCacheStorageService {

    boolean putTx(Transaction tx);

    Transaction getTx(NulsDigestData hash);

    boolean removeTx(NulsDigestData hash);

    int getMaxIndex();

    Transaction pollTx();
}
