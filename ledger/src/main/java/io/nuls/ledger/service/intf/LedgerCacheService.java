package io.nuls.ledger.service.intf;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.ledger.constant.TxBroadCastStatusEnum;
import io.nuls.ledger.entity.Balance;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface LedgerCacheService {

    void putTx(Transaction transaction);

    Transaction getTx(String hashHex);

    List<Transaction> getTxList();

    List<Transaction> getTxList(long startTime, long endTime);

    void removeTx(String hashHex);

    TxBroadCastStatusEnum getTxBroadCastStatus(String hashHex);

    Balance getBalance(String address);
}