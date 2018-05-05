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

import io.nuls.consensus.poc.entity.TestTransaction;
import io.nuls.consensus.service.ConsensusServiceIntf;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
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

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void newTx() throws Exception {

        assertNotNull(service);

        // new a tx
        Transaction tx = new TestTransaction();
        tx.setTime(0l);

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertEquals(tx.getHash().getDigestHex(), "0800122099a2986481ecb269ee9bf8780130502d19505a563165745e65314b455d6bc976");

        Result result = service.newTx(tx);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());

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

        newTx();

        List<Transaction> memoryTxs = service.getMemoryTxs();
        assertNotNull(memoryTxs);

        assertEquals(1, memoryTxs.size());

        Transaction tx = memoryTxs.get(0);
        assertNotNull(tx);
        assertEquals(tx.getHash().getDigestHex(), "0800122099a2986481ecb269ee9bf8780130502d19505a563165745e65314b455d6bc976");
    }

    @Test
    public void reset() throws Exception {

        service.reset();
    }

}