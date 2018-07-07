package io.nuls.consensus.poc.storage.service;

import io.nuls.kernel.model.Transaction;

public interface TransactionQueueStorageService {

    boolean putTx(Transaction tx);

    Transaction pollTx();
}
