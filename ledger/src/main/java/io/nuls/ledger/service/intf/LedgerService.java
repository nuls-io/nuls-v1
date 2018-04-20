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
package io.nuls.ledger.service.intf;

import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.Result;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.Balance;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/9
 */
public interface LedgerService {

    Transaction getTx(NulsDigestData hash);

    Transaction getTx(String fromHash, int fromIndex);

    List<Transaction> getTxList(String address, int txType) throws Exception;

    long getTxCount(Long blockHeight, String address, int txType) throws Exception;

    List<Transaction> getTxList(Long blockHeight, String address, int txType, int pageNumber, int pageSize) throws Exception;

    List<Transaction> getTxList(String blockHash) throws Exception;

    List<Transaction> getTxList(long startHeight, long endHeight) throws Exception;

    List<Transaction> getTxList(long height) throws Exception;

    Page<Transaction> getTxList(Long height, int type, int pageNum, int pageSize) throws Exception;

    Balance getBalance(String address);

    Balance getBalances();

    Na getTxFee(int txType);

    Result transfer(String address, String password, String toAddress, Na amount, String remark);

    Result transfer(List<String> addressList, String password, String toAddress, Na amount, String remark);

    /**
     * unlockTime < 100,000,000,000  means the blockHeight
     * unlockTime > 100,000,000,000  means the timestamp
     */
    Result lock(String address, String password, Na amount, long unlockTime, String remark);

    void saveTxList(List<Transaction> txList) throws IOException;

    boolean saveLocalTx(Transaction tx) throws IOException;

    void deleteLocalTx(String txHash);

    void saveTxInLocal(String address);

    boolean checkTxIsMine(Transaction tx);

    boolean checkTxIsMine(Transaction tx, String address);

    boolean checkTxIsMySend(Transaction tx);

    boolean checkTxIsMySend(Transaction tx, String address);

    void rollbackTx(Transaction tx, Block block) throws NulsException;

    void commitTx(Transaction tx, Block block) throws NulsException;

    ValidateResult conflictDetectTx(Transaction tx, List<Transaction> txList) throws NulsException;

    List<Transaction> getWaitingTxList();

    void deleteTx(Transaction tx);

    void deleteTx(long blockHeight);

    long getBlockReward(long blockHeight);

    Long getBlockFee(Long blockHeight);

    /**
     * get the last 24 hours coinbase transaction reward
     */
    long getLastDayTimeReward();

    long getAccountReward(String address, long lastTime);

    long getAgentReward(String address, int type);

    void unlockTxApprove(String txHash);

    void unlockTxSave(String txHash);

    void unlockTxRollback(String txHash);

    Page getLockUtxo(String address, Integer pageNumber, Integer pageSize);

    void resetLedgerCache();
}
