/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.service;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.TestTransaction;
import io.nuls.consensus.service.ConsensusServiceIntf;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.TransactionValidatorManager;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/5.
 */
public class ConsensusPocServiceTest {

    private ConsensusServiceIntf service = new ConsensusPocService();

    @Before
    public void setUp() throws Exception {
        TxMemoryPool.getInstance().clear();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void newTx() throws Exception {

        assertNotNull(service);

        // new a tx
        Transaction tx = new TestTransaction();
        tx.setTime(1l);

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertEquals(tx.getHash().getDigestHex(), "0800122077b35f4a04e6d3e49521ef7fc4d80515bde810114a0f714f3b6d5b357bda0a0d");

        Result result = service.newTx(tx);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());

        //test orphan
        NulsDataValidator<TestTransaction> testValidator = new NulsDataValidator<TestTransaction>() {
            @Override
            public ValidateResult validate(TestTransaction data) {
                if(data.getHash().getDigestHex().equals("0800122077b35f4a04e6d3e49521ef7fc4d80515bde810114a0f714f3b6d5b357bda0a0d")) {
                    return ValidateResult.getFailedResult("test.transaction", TransactionErrorCode.ORPHAN_TX);
                } else {
                    return ValidateResult.getSuccessResult();
                }
            }
        };
        TransactionValidatorManager.addTxDefValidator(testValidator);

        tx = new TestTransaction();
        tx.setTime(1l);
        result = service.newTx(tx);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());

        List<Transaction> list = TxMemoryPool.getInstance().getAll();
        assertNotNull(list);
        assertEquals(list.size(), 1);

        List<Transaction> orphanList = TxMemoryPool.getInstance().getAllOrphan();
        assertNotNull(orphanList);
        assertEquals(orphanList.size(), 1);

    }

    @Test
    public void newBlock() throws Exception {
    }

    @Test
    public void addBlock() throws Exception {
    }

    @Test
    public void rollbackBlock() throws Exception {
    }

    @Test
    public void getMemoryTxs() throws Exception {

        assertNotNull(service);

        Transaction tx = new TestTransaction();
        tx.setTime(0l);

        assertEquals(tx.getHash().getDigestHex(), "08001220d194faf5b314f54c3a299ca9ea086f3f8856c75dc44a1e0c43bcc4d80b47909c");

        Result result = service.newTx(tx);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());

        List<Transaction> memoryTxs = service.getMemoryTxs();
        assertNotNull(memoryTxs);

        assertEquals(1, memoryTxs.size());

        tx = memoryTxs.get(0);
        assertNotNull(tx);
        assertEquals(tx.getHash().getDigestHex(), "08001220d194faf5b314f54c3a299ca9ea086f3f8856c75dc44a1e0c43bcc4d80b47909c");
    }

    @Test
    public void reset() throws Exception {

        service.reset();
    }

}