package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    private static final LedgerService instance = new UtxoLedgerServiceImpl();

    private UtxoLedgerServiceImpl() {
    }

    public static LedgerService getInstance() {
        return instance;
    }

    @Override
    public Balance getBalance(String address) {
        //todo
        return null;
    }

    @Override
    public boolean transfer(Address address, String password, Address toAddress, double amount, String remark) {
        //todo
        return false;
    }

    @Override
    public boolean saveTransaction(Transaction tx) {
        //todo
        return false;
    }

    @Override
    public Transaction query(byte[] txid) {
        //todo
        return null;
    }

    @Override
    public List<Transaction> queryListByAccount(String address, int txType, long beginTime) {
        //todo
        return null;
    }

}
