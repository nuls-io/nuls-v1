package io.nuls.ledger.service.intf;

import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface TxCacheService {

    void putTx(Transaction transaction);
    Transaction getTx(String hashHex);
    List<Transaction> getTxList();
    List<Transaction> getTxList(long startTime, long endTime);

}
