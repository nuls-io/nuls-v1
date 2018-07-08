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

package io.nuls.protocol.base.utils.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Niels Wang
 * @date: 2018/7/8
 */
public class InventoryFilterTest {

    private AtomicInteger count = new AtomicInteger(0);

    @Test
    public void test1() {
        BloomFilter<byte[]> filter = BloomFilter.create(Funnels.byteArrayFunnel(), 1000000, 0.00001);
        ArrayList<Transaction> txList = new ArrayList<>();
        for (int i = 1000000; i < 2000000; i++) {
            Transaction tx = new TransferTransaction();
            tx.setTime(i);
            tx.setRemark("sdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaa".getBytes());
            txList.add(tx);
        }
        System.out.println("start....");
        long start = System.currentTimeMillis();
        for (Transaction tx : txList) {
            NulsDigestData hash = tx.getHash();
            if (!filter.mightContain(hash.getDigestBytes())) {
                filter.put(hash.getDigestBytes());
                int num = count.incrementAndGet();
                if (num % 100 == 0) {
                    System.out.println("count::::::" + num);
                }
            }
        }
        System.out.println("use time::" + (System.currentTimeMillis() - start));
        System.out.println(count.get());

    }

    @Test
    public void test() throws IOException {

        BloomFilter<byte[]> filter = BloomFilter.create(Funnels.byteArrayFunnel(), 1000000, 0.00001);
        List<String> list = new ArrayList<>();
        Set<NulsDigestData> set = new HashSet<>();
        ArrayList<Transaction> txList = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            Transaction tx = new TransferTransaction();
            tx.setTime(i);
            tx.setRemark("sdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaa".getBytes());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            txList.add(tx);
        }
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Transaction tx : txList) {
                        NulsDigestData hash = tx.getHash();
                        if (!filter.mightContain(hash.getDigestBytes())) {
                            filter.put(hash.getDigestBytes());
                            set.add(hash);
                            int num = count.incrementAndGet();
                            if (num % 1000 == 0) {
                                System.out.println("count::::::" + num);
                            }
                        }
                    }
                    list.add("done");
                }
            });
            t.start();
        }
        while (list.size() < 5) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("count====" + count.get());
        System.out.println("real-size====" + set.size());

    }
}