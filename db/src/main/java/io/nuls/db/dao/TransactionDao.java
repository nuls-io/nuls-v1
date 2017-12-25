package io.nuls.db.dao;

import io.nuls.db.entity.TransactionPo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface TransactionDao extends BaseDao<String, TransactionPo> {

    List<TransactionPo> getTxs(Long blockHeight);

    List<TransactionPo> getTxs(String blockHash);

    List<TransactionPo> getTxs(byte[] blockHash);

    List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize);

    List<TransactionPo> listTranscation(int limit, String address);

    List<TransactionPo> listTransaction(long blockHeight, String address);

}
