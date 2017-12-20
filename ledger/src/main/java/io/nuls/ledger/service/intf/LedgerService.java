package io.nuls.ledger.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/9
 *
 */
public interface LedgerService {

    ValidateResult verifyAndCacheTx(Transaction tx) throws NulsException;

    Transaction getTxFromCache(String hash);

    boolean txExist(String hash);

    Balance getBalance(String address);

    boolean transfer(Address address, String password, Address toAddress, double amount, String remark);

    boolean saveTransaction(Transaction tx);

    Transaction query(byte[] txid);

    List<Transaction> queryListByAccount(String address, int txType, long beginTime);

    boolean lockNuls(String address, String password, Na na);

    LockNulsTransaction createLockNulsTx(String address, String password, Na na);

    Transaction getTransaction(NulsDigestData txHash);

    UnlockNulsTransaction createUnlockTx(LockNulsTransaction lockNulsTransaction);

    List<Transaction> queryListByHashs(List<NulsDigestData> txHashList);
}
