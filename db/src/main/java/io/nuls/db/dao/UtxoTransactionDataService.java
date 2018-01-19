package io.nuls.db.dao;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public interface UtxoTransactionDataService {

    TransactionPo gettx(String hash, boolean isLocal);

    List<TransactionPo> getTxs(long blockHeight, boolean isLocal);

    List<TransactionPo> getTxs(long startHeight, long endHeight, boolean isLocal);

    List<TransactionPo> getTxs(String blockHash, boolean isLocal);

    List<TransactionPo> getTxs(byte[] blockHash, boolean isLocal);

    List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal);

    List<TransactionPo> getTxs(String address, int type, boolean isLocal);

    List<TransactionPo> listTranscation(int limit, String address, boolean isLocal);

    List<TransactionPo> listTransaction(long blockHeight, String address, boolean isLocal);

    List<UtxoInputPo> getTxInputs(String txHash);

    List<UtxoOutputPo> getTxOutputs(String txHash);

    List<UtxoOutputPo> getAccountOutputs(String address, byte status);

    List<UtxoOutputPo> getAccountUnSpend(String address);

    int save(TransactionPo po);

    int save(TransactionLocalPo localPo);

    int saveTxList(List<TransactionPo> poList);

    int saveLocalList(List<TransactionLocalPo> poList);
}
