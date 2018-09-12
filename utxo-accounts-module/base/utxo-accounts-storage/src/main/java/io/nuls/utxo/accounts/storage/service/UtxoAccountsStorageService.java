package io.nuls.utxo.accounts.storage.service;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.utxo.accounts.storage.po.LocalCacheBlockBalance;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsBalancePo;


import java.util.List;

public interface UtxoAccountsStorageService {

    Result saveUtxoAccountsInfo(byte[] addressBytes, UtxoAccountsBalancePo balance);
//    public Result<UtxoAccountsBalance> getUtxoAccountBalanceByAddress(byte[] addressBytes);
    Result saveByteUtxoAccountsInfo(byte[] addressBytes, UtxoAccountsBalancePo balance);
    Result batchSaveByteUtxoAcountsInfo(List<UtxoAccountsBalancePo> list);
    Result deleteUtxoAcountsInfo(byte[] addressBytes);
    long getHadSynBlockHeight();
    Result saveHadSynBlockHeight(long height);
    Result<UtxoAccountsBalancePo> getUtxoAccountsBalanceByAddress(byte[] addressBytes) throws NulsException;

    /**
     * 根据区块高度获取区块（从存储中）
     * Get the block (from storage) according to the block height
     *
     * @param height 区块高度/block height
     * @return 区块/block
     */
    Result<LocalCacheBlockBalance> getLocalCacheBlock(long height) throws NulsException;

    Result saveLocalCacheBlock(long height, LocalCacheBlockBalance localCacheBlockBalance);

    Result deleteLocalCacheBlock(long height);

    Transaction getTx(NulsDigestData hash);
    Result<Block> getBlock(long height);


}