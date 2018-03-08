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

    List<TransactionPo> getTxs(String blockHash);

    List<TransactionPo> getTxs(long startHeight, long endHeight);

    Long getTxsCount(String address, int type);

    List<TransactionPo> getTxs(String address, int type, Integer pageNumber, Integer pageSize);

    List<TransactionLocalPo> getLocalTxs(long blockHeight);

    List<TransactionLocalPo> getLocalTxs(String blockHash);

    List<TransactionLocalPo> getLocalTxs(long startHeight, long endHeight);

    List<TransactionLocalPo> getLocalTxs(String address, int type, Integer start, Integer limit);

    Long getLocalTxsCount(String address, int type);

    List<UtxoInputPo> getTxInputs(String txHash);

    List<UtxoOutputPo> getTxOutputs(String txHash);

    List<UtxoOutputPo> getAccountOutputs(String address, byte status);

    List<UtxoOutputPo> getAccountUnSpend(String address);

    int save(TransactionPo po);

    int save(TransactionLocalPo localPo);

    int saveTxList(List<TransactionPo> poList);

    int saveLocalList(List<TransactionLocalPo> poList);
}
