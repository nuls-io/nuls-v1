/**
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
 */
package io.nuls.db.dao;

import io.nuls.core.dto.Page;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public interface UtxoTransactionDataService {

    TransactionPo gettx(String hash);

    TransactionLocalPo getLocaltx(String hash);

    List<TransactionPo> getTxs(long blockHeight);

    Page<TransactionPo> getTxs(Long blockHeight, int type, int pageNum, int pageSize);

    List<TransactionPo> getTxs(String blockHash);

    List<TransactionPo> getTxs(long startHeight, long endHeight);

    Long getTxsCount(Long blockHeight, String address, int type);

    List<TransactionPo> getTxs(Long blockHeight, String address, int type, int start, int limit);

    List<TransactionLocalPo> getLocalTxs(long blockHeight);

    List<TransactionLocalPo> getLocalTxs(String blockHash);

    List<TransactionLocalPo> getLocalTxs(long startHeight, long endHeight);

    List<TransactionLocalPo> getLocalTxs(Long blockHeight, String address, int type, int start, int limit);

    Long getLocalTxsCount(Long blockHeight, String address, int type);

    List<UtxoInputPo> getTxInputs(String txHash);

    UtxoInputPo getTxInput(String fromHash, int fromIndex);

    List<UtxoOutputPo> getTxOutputs(String txHash);


    List<UtxoOutputPo> getAccountOutputs(String address, byte status);

    List<UtxoOutputPo> getAccountUnSpend(String address);

    int save(TransactionPo po);

    int save(TransactionLocalPo localPo);

    int saveTxList(List<TransactionPo> poList);

    int saveLocalList(List<TransactionLocalPo> poList);

    void deleteTx(String txHash);

    long getBlockReward(long blockHeight);

    Long getBlockFee(long blockHeight);

    long getLastDayTimeReward();

    long getAccountReward(String address, long lastTime);

    long getAgentReward(String address, int type);

    void unlockTxOutput(String txHash);

    void lockTxOutput(String txHash);
}
