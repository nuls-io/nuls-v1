package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
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
    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private LedgerServiceImpl() {
    }

    public static LedgerService getInstance() {
        return instance;
    }

    @Override
    public Balance getBalance(String address) {
        Balance balance = ledgerCacheService.getBalance(address);
        if(null==balance){
            balance = calcBalance(address);
            ledgerCacheService.putBalance(address,balance);
        }
        return balance;
    }

    private Balance calcBalance(String address) {
        Balance balance = new Balance();
        //todo use dao
        return balance;
    }

    @Override
    public boolean transfer(Address address, String password, Address toAddress, double amount, String remark) {
        //todo
        return false;
    }

    @Override
    public boolean saveTransaction(Transaction tx) {
        boolean result = false;
        do{
            if(null==tx){
                break;
            }
            try{
                tx.verify();
            }catch (NulsVerificationException e){
                break;
            }
            //todo save to db
        }while(false);
        return result;
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
