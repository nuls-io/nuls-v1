package io.nuls.db.dao;

import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public interface UtxoTransactionDao {

    TransactionPo gettx(String hash, boolean isLocal);

    List<TransactionPo> getTxs(Long blockHeight, boolean isLocal);

    List<TransactionPo> getTxs(String blockHash, boolean isLocal);

    List<TransactionPo> getTxs(byte[] blockHash, boolean isLocal);

    List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal);

    List<TransactionPo> listTranscation(int limit, String address, boolean isLocal);

    List<TransactionPo> listTransaction(long blockHeight, String address, boolean isLocal);

    List<UtxoInputPo> getTxInputs(String txhash);

    List<UtxoOutputPo> getTxOutputs(String txhash);

    List<UtxoOutputPo> getUnSpend(String address);
}
