/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
