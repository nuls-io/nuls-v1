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

package io.nuls.protocol.service;

import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/9
 */
public class TransactionServiceTest {

    private List<Transaction> txList ;

    /**
     * 初始化交易列表，交易中包含双花交易和全部互相冲突的交易
     * Initializing a list of trades involving double - flower transactions and all conflicting transactions.
     */
    @Before
    public void init(){
        TransferTransaction tt = new TransferTransaction();
        tt.setTime(System.currentTimeMillis());
        tt.setBlockHeight(1);
//        tt.setScriptSig();
    }

    @Test
    public void commitTx() {
    }

    @Test
    public void rollback() {
    }

    @Test
    public void forwardTx() {
    }

    @Test
    public void broadcastTx() {
    }

    @Test
    public void conflictDetect() {


    }
}