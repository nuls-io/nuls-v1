/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
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