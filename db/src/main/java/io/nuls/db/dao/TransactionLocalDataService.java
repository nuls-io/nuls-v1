package io.nuls.db.dao;

import io.nuls.db.entity.TransactionLocalPo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface TransactionLocalDataService extends BaseDataService<String, TransactionLocalPo> {

    List<TransactionLocalPo> getTxs(Long blockHeight);

    List<TransactionLocalPo> getTxs(Long startHeight, Long endHeight);

    List<TransactionLocalPo> getTxs(String blockHash);

    List<TransactionLocalPo> getTxs(byte[] blockHash);

    List<TransactionLocalPo> getTxs(String address, int type, int pageNum, int pageSize);

    List<TransactionLocalPo> getTxs(String address, int type);

    List<TransactionLocalPo> listTranscation(int limit, String address);

    List<TransactionLocalPo> listTransaction(long blockHeight, String address);
}
