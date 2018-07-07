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

package io.nuls.kernel.utils.queue.entity;

import io.nuls.kernel.model.Transaction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/7/8
 */
public class PersistentQueueTest {

    private List<Transaction> txList;

    @Test
    public void test() throws Exception {
//        FQueue  txQueue = new FQueue("tx-1", 1000001L);
        PersistentQueue  txQueue = new PersistentQueue("tx-12", 1000001L);
        this.txList = new ArrayList<>();
        for (int i = 0; i <1000000; i++) {
            Transaction tx = new TestTransaction();
            tx.setTime(1);
            tx.setRemark("sdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaa".getBytes());
            txList.add(tx);
        }


        long start = System.currentTimeMillis();
        for (Transaction tx : txList) {
            txQueue.offer(tx.serialize());
        }
        System.out.println("存入100万条用时：" + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i ++) {
            txQueue.poll();
        }
        System.out.println("取出100万次用时：" + (System.currentTimeMillis() - start) + "ms");

        assertTrue(true);
    }
}