package io.nuls.ledger.service.impl;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.db.dao.UtxoTransactionDataService;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.util.TransactionPoTool;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoLedgerServiceImpl implements LedgerService {

    private static final LedgerService INSTANCE = new UtxoLedgerServiceImpl();

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private UtxoTransactionDataService txDao = NulsContext.getInstance().getService(UtxoTransactionDataService.class);

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    private UtxoLedgerServiceImpl() {

    }

    public static LedgerService getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult verifyTx(Transaction tx)   {
        AssertUtil.canNotEmpty(tx,ErrorCode.NULL_PARAMETER);
        return tx.verify();
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
            TransactionPo po = txDao.gettx(hash.getDigestHex(), false);
            try {
                return TransactionPoTool.toTransaction(po);
            } catch (Exception e) {
                Log.error(e);
            }
        return null;
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
        UtxoBalance balance = new UtxoBalance();
        List<UtxoOutputPo> unSpendList = txDao.getAccountOutputs(address, TransactionConstant.TX_OUTPUT_UNSPEND);
        List<UtxoOutput> unSpends = new ArrayList<>();

        long value = 0;
        for (UtxoOutputPo output : unSpendList) {
            value += output.getValue();

            //todo
        }
        balance.setUseable(Na.valueOf(value));

        List<UtxoOutputPo> lockedList = txDao.getAccountOutputs(address, TransactionConstant.TX_OUTPUT_LOCKED);
        value = 0;
        for (UtxoOutputPo output : lockedList) {
            value += output.getValue();
        }
        balance.setLocked(Na.valueOf(value));
        balance.setBalance(balance.getLocked().add(balance.getUseable()));
//        balance.setUnSpends(unSpendList);
        return balance;
    }


//    @Override
//    public Result transfer(Account account, String password, Address toAddress, Na amount, String remark) {
//        if (account == null) {
//            return new Result(false, "account not found");
//        }
//        if (!account.validatePassword(password)) {
//            return new Result(false, "password error");
//        }
//        Balance balance = getBalance(account.getAddress().getBase58());
//        if (balance.getUseable().isLessThan(amount)) {
//            return new Result(false, "balance is not enough");
//        }
//        try {
//            CoinTransferData coinData = new CoinTransferData(amount, account.getAddress().getBase58(), toAddress.getBase58());
//            TransferTransaction tx = new TransferTransaction(coinData, password);
//            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
//            aliasTx.setSign(signData(aliasTx.getHash(), account, password));
////            tx.setSign();
////            TransferCoinEvent
//        } catch (IOException e) {
//            Log.error(e);
//            return new Result(false, "transaction failed");
//        } catch (Exception e) {
//            Log.error(e);
//            return new Result(false, e.getMessage());
//        }
//
//        return null;
//    }

    @Override
    @TransactionalAnnotation
    public boolean saveTx(Transaction tx) {
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
    public Transaction getLocalTx(NulsDigestData hash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public LockNulsTransaction lock(String address, String password, Na amount, long unlockTime, long unlockHeight) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public TransferTransaction transfer(String address, String password, String toAddress, Na amount, String remark) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    @TransactionalAnnotation
    public boolean saveTxList(List<Transaction> txList) {
        // todo auto-generated method stub(niels)
        return false;
    }

    @Override
    public List<Transaction> getListByAddress(String address, int txType, long beginTime, long endTime) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public List<Transaction> getListByHashs(List<NulsDigestData> txHashList) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public void rollbackTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onRollback(tx);
        }
    }
    @Override
    public void commitTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onCommit(tx);
        }
    }
    @Override
    public void approvalTx(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onApproval(tx);
        }
    }

    public List<TransactionService> getServiceList(Class<? extends Transaction> txClass) {
        List<TransactionService> list = new ArrayList<>();
        Class clazz = txClass;
        while (!clazz.equals(Transaction.class)) {
            TransactionService txService = TransactionManager.getService(clazz);
            if (null != txService) {
                list.add(0, txService);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }
}
