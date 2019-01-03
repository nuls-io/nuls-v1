/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.cache;

import io.nuls.consensus.poc.TestTransaction;
import io.nuls.kernel.model.Transaction;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/6.
 */
public class TxMemoryPoolTest {

    private TxMemoryPool txMemoryPool = TxMemoryPool.getInstance();

    @Test
    public void test() {
        assertNotNull(txMemoryPool);

        Transaction tx = new TestTransaction();

        boolean success = txMemoryPool.add(tx, false);

        assertTrue(success);

        Transaction tempTx = txMemoryPool.get();

        assertEquals(tx, tempTx);

        tempTx = txMemoryPool.get();

        assertNull(tempTx);

//        tempTx = txMemoryPool.get(tx.getTx().getHash());
//        assertNull(tempTx);
//
//        txMemoryPool.add(tx, false);
//        tempTx = txMemoryPool.get(tx.getTx().getHash());
//        assertEquals(tempTx, tx);
//
//        tempTx = txMemoryPool.get(tx.getTx().getHash());
//        assertEquals(tempTx, tx);

        Transaction tx2 = new TestTransaction();
        txMemoryPool.add(tx2, true);

        tempTx = txMemoryPool.get();

        assertNotNull(tempTx);
        assertEquals(tempTx, tx);
        tempTx = txMemoryPool.get();

        assertNotNull(tempTx);
        assertEquals(tempTx, tx2);

        tempTx = txMemoryPool.get();
        assertNull(tempTx);

        txMemoryPool.add(tx2, true);
        List<Transaction> list = txMemoryPool.getAll();
        assertEquals(list.size(), 0);

        list = txMemoryPool.getAllOrphan();
        assertEquals(list.size(), 1);

        success = txMemoryPool.exist(tx.getHash());
        assertFalse(success);

        success = txMemoryPool.exist(tx2.getHash());
        assertTrue(success);

        success = txMemoryPool.remove(tx2.getHash());
        assertTrue(success);

        success = txMemoryPool.exist(tx2.getHash());
        assertFalse(success);
    }
}