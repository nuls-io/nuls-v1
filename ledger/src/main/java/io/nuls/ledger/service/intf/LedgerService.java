package io.nuls.ledger.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.ledger.entity.Balance;

import java.util.List;

/**
 * Created by Niels on 2017/11/9.
 *
 */
public interface LedgerService {

    Balance getBalance(String address);



    boolean transfer(Address address, String password, Address toAddress, double amount, String remark);

    boolean saveTransaction(Transaction tx);

    Transaction query(byte[] txid);

    List<Transaction> queryListByAccount(String address, int txType, long beginTime);

}
