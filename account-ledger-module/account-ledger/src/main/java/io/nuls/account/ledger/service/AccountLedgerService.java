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

package io.nuls.account.ledger.service;

import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.model.Account;
import io.nuls.account.model.Balance;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;

import java.util.List;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
public interface AccountLedgerService {

    /**
     * <p>
     * save a confirmed tx to account ledger.
     * save if the tx is relative to local accounts, or do nothing.
     *
     * @param tx transaction to save
     * @return return the tx count saved,
     */
    Result<Integer> saveConfirmedTransaction(Transaction tx);

    /**
     * <p>
     * save a unconfirmed tx to account ledger.
     * save if the tx is relative to local accounts, or do nothing.
     *
     * @param tx transaction to save
     * @return return the tx count saved,
     */
    Result<Integer> saveUnconfirmedTransaction(Transaction tx);


    /**
     * <p>
     * save a tx to account ledger.
     * save if the tx is relative to local accounts, or do nothing.
     *
     * @param txs transactions to save
     * @return return the tx count saved,
     */
    Result<Integer> saveConfirmedTransactionList(List<Transaction> txs);


    /**
     * <p>
     * get an .unconfirmed transaction
     *
     * @param hash transaction hash
     * @return return the unconfirmed tx
     */
    Result<Transaction> getUnconfirmedTransaction(NulsDigestData hash);

    /**
     * <p>
     * get all.unconfirmed transactions
     *
     * @return return all the unconfirmed txs
     */
    public Result<List<Transaction>> getAllUnconfirmedTransaction();

    /**
     * <p>
     * rollback a tx in account ledger
     * save if the tx is relative to local accounts, or do nothing
     *
     * @param tx transaction to rollback
     * @return return the tx count rollbacked
     */
    Result<Integer> rollback(Transaction tx);

    /**
     * <p>
     * rollback a tx list in account ledger.
     * save if the tx is relative to local accounts, or do nothing
     *
     * @param txs transactions to rollback
     * @return return the tx count rollbacked
     */
    Result<Integer> rollback(List<Transaction> txs);

    /**
     * <p>
     * get the balance of an local account.
     *
     * @param address account address
     * @return return balance of account, return 0 if  account is not a local account
     */
    Result<Balance> getBalance(byte[] address) throws NulsException;

    /**
     * <p>
     * get useable coindata
     *
     * @param address account address
     * @param amount  amount want to use
     * @param size    size of transaction ,to calc the fee
     * @return return balance of account, return 0 if  account is not a local account
     */
    CoinDataResult getCoinData(byte[] address, Na amount, int size) throws NulsException;

    Transaction getTxByOwner(byte[] owner);

    /**
     * <p>
     * check weather address is a local address
     *
     * @param address account address
     * @return true if a address is a local address
     */
    boolean isLocalAccount(byte[] address);

    /**
     * @param from
     * @param to
     * @param values
     */
    Result transfer(byte[] from, byte[] to, Na values, String password, String remark);

    /**
     * <p>
     * get local address list
     *
     * @param tx
     * @return true if a address is a local address
     */
    Result unlockCoinData(Transaction tx);

    Result rollbackUnlockTxCoinData(Transaction tx);

    /**
     * <p>
     * load the local ledger of a account when an account imported
     *
     * @param address
     * @return true if a address is a local address
     */
    Result importAccountLedger(String address);

    /**
     * 查询交易列表
     *
     * @param address
     * @return
     */
    Result<List<TransactionInfo>> getTxInfoList(byte[] address);

    /**
     * 查询锁定的未花费交易
     *
     * @param address
     * @return
     */
    Result<List<Coin>> getLockUtxo(byte[] address);
}
