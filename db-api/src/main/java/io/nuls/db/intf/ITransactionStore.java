package io.nuls.db.intf;

import io.nuls.db.entity.Transaction;

import java.util.List;

/**
 * Created by zhouwei on 2017/10/17.
 */
public interface ITransactionStore extends IStore<Transaction, String> {

    /**
     * 根据区块高度获取区块的所有交易
     * @param blockHeight
     * @return
     */
    List<Transaction> getTxs(Long blockHeight);


    /**
     * 根据区块hash获取区块的所有交易
     * @param blockHash
     * @return
     */
    List<Transaction> getTxs(String blockHash);

    /**
     * 根据区块hash获取区块的所有交易
     * @param blockHash
     * @return
     */
    List<Transaction> getTxs(byte[] blockHash);

    /**
     * 根据交易hash获取交易记录
     * @param txhash
     * @return
     */
    Transaction getByKey(byte[] txhash);

    /**
     * 获取账户的交易记录
     * @param address 账户地址值
     * @param type 交易类型， 0为所有类型
     * @param pageNum 当前页
     * @param pageSize 每页显示条数
     * @param isLocal 是否是本地账户
     * @return
     */
    List<Transaction> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal);


    /**
     * 为交易所提供的查询代币交易接口
     * @param limit 查询条数，最大支持1000条，按交易日期倒叙排列
     * @param address 本地账户地址，不传默认取全部账户
     * @return
     */
    List<Transaction> listTranscation(int limit, String address);

    /**
     * 为交易所提供的查询代币交易接口
     * @param blockHeight 区块高度
     * @param address 本地账户地址，不传默认取全部账户
     * @return
     */
    List<Transaction> listTransaction(long blockHeight, String address);


}
