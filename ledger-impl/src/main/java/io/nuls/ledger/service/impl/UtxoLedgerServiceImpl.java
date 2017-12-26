package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.UtxoTransactionDao;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.TransactionTool;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    private static final LedgerService INSTANCE = new UtxoLedgerServiceImpl();

    private LedgerCacheServiceImpl ledgerCacheService = LedgerCacheServiceImpl.getInstance();

    private UtxoTransactionDao txDao = NulsContext.getInstance().getService(UtxoTransactionDao.class);

    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private UtxoLedgerServiceImpl() {
    }

    public static LedgerService getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult verifyAndCacheTx(Transaction tx) throws NulsException {
        ValidateResult result = tx.verify();
        if (result.isFailed()) {
            return result;
        }
        tx.onApproval();
        ledgerCacheService.putTx(tx);
        return result;
    }

    @Override
    public Transaction getTxFromCache(String hash) {
        Transaction tx = ledgerCacheService.getTx(hash);
        return tx;
    }

    @Override
    public Transaction gettx(byte[] txid, boolean isMine) {
        String hash = Hex.encode(txid);
        return gettx(hash, isMine);
    }

    @Override
    public Transaction gettx(String hash, boolean isMine) {
        Transaction tx = getTxFromCache(hash);
        if (tx == null) {
            TransactionPo po = txDao.gettx(hash, isMine);
            try {
                tx = TransactionTool.toTransaction(po);
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return tx;
    }

    @Override
    public boolean txExist(String hash) {
        Transaction tx = getTxFromCache(hash);
        if (tx != null) {
            return true;
        }
        TransactionPo po = txDao.gettx(hash, false);
        return po != null;
    }

    @Override
    public Balance getBalance(String address) {
        Balance balance = ledgerCacheService.getBalance(address);
        if (null == balance) {
            balance = calcBalance(address);
            ledgerCacheService.putBalance(address, balance);
        }
        return balance;
    }

    private Balance calcBalance(String address) {
        Balance balance = new Balance();
        List<UtxoOutputPo> unSpendList = txDao.getAccountOutputs(address, TransactionConstant.TX_OUTPUT_UNSPEND);
        long value = 0;
        for (UtxoOutputPo output : unSpendList) {
            value += output.getValue();
        }
        balance.setUseable(Na.valueOf(value));

        List<UtxoOutputPo> lockedList = txDao.getAccountOutputs(address, TransactionConstant.TX_OUTPUT_LOCKED);
        value = 0;
        for (UtxoOutputPo output : lockedList) {
            value += output.getValue();
        }
        balance.setLocked(Na.valueOf(value));
        balance.setBalance(balance.getLocked().add(balance.getUseable()));
        return balance;
    }

    @Override
    public Result transfer(Address address, String password, Address toAddress, Na amount, String remark) {
        Account account = accountService.getAccount(address.getBase58());
        if(account == null) {
            return new Result(false, "account not found");
        }
        if(!account.validatePassword(password)) {
            return new Result(false, "password error");
        }
        Balance balance = getBalance(address.getBase58());
        if(balance.getUseable().isLessThan(amount)) {
            return new Result(false, "balance is not enough");
        }

        TransferTransaction tx = new TransferTransaction();

        return null;
    }

    @Override
    public boolean saveTransaction(Transaction tx) {
        boolean result = false;
        do {
            if (null == tx) {
                break;
            }
            try {
                tx.verify();
            } catch (NulsVerificationException e) {
                break;
            }
            //todo save to db
        } while (false);
        return result;
    }

    @Override
    public List<Transaction> queryListByAccount(String address, int txType, long beginTime) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> queryPoListByAccount(String address, int txType, long beginTime) {

        return null;
    }

    @Override
    public boolean lockNuls(String address, String password, Na na) {
        // todo auto-generated method stub(niels)
        return false;
    }

    @Override
    public Transaction getTransaction(NulsDigestData txHash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public List<Transaction> queryListByHashs(List<NulsDigestData> txHashList) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public void removeFromCache(List<NulsDigestData> txHashList) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public List<Transaction> getTxListFromCache() {
        // todo auto-generated method stub(niels)
        return null;
    }

    private UtxoData getUtxoData(Na na) {
        //todo
        return null;
    }
}
