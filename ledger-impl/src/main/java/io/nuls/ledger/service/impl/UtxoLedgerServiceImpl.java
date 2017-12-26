package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.UtxoTransactionDao;
import io.nuls.db.entity.TransactionPo;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.TransactionTool;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.entity.tx.UtxoLockTransaction;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    private static final LedgerService INSTANCE = new UtxoLedgerServiceImpl();

    private LedgerCacheServiceImpl ledgerCacheService = LedgerCacheServiceImpl.getInstance();

    private UtxoTransactionDao txdao = NulsContext.getInstance().getService(UtxoTransactionDao.class);

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
            TransactionPo po = txdao.gettx(hash, isMine);
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
        if(tx != null) {
            return true;
        }
        TransactionPo po = txdao.gettx(hash, false);
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

    private UtxoData getUtxoData(Na na) {
        //todo
        return null;
    }
}
