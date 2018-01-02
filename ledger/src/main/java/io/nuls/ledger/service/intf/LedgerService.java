package io.nuls.ledger.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.TransactionPo;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.tx.TransferTransaction;

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

    Transaction getTx(byte[] txid, boolean isMine);

    Transaction getTx(String hash, boolean isMine);

    boolean txExist(String hash);

    Balance getBalance(String address);

    Result transfer(TransferTransaction tx);

    //Result transfer(Address address, String password, Address toAddress, Na amount, String remark);

    boolean saveTransaction(Transaction tx);

    List<Transaction> queryListByAccount(String address, int txType, long beginTime);

    List<TransactionPo> queryPoListByAccount(String address, int txType, long beginTime);

    Transaction getTransaction(NulsDigestData txHash);

    List<Transaction> queryListByHashs(List<NulsDigestData> txHashList);

    void removeFromCache(List<NulsDigestData> txHashList);

    List<Transaction> getTxListFromCache();
}
