/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.db.service;

import io.nuls.db.entity.TestTransaction;
import io.nuls.db.service.impl.LevelDBServiceImpl;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.nuls.db.manager.LevelDBManager.createArea;

/**
 * @author: Niels
 * @date: 2018/7/7
 */
public class PerformanceTest {

    private static DBService dbService;

    private static String areaName = "transaction-1";

    private List<Transaction> txList;

    @BeforeClass
    public static void init() throws Exception {
        dbService = new LevelDBServiceImpl();
        dbService.destroyArea(areaName);
        dbService.createArea(areaName);
    }

    @Test
    public void test() throws IOException {
        initData();
    }

    private void initData() throws IOException {
        this.txList = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            Transaction tx = new TestTransaction();
            tx.setTime(1);
            tx.setRemark("sdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaaadsfasdfsadfsdfasdfasdfasdfasdfasdfsadfaaaaaaaaaaaaaaaaaaaaaabsdsadfsadfsdfsdfsdfsdfsdfsdfsdfaa".getBytes());
            txList.add(tx);
        }

        long start = System.currentTimeMillis();
        for (Transaction tx : txList) {
            dbService.putModel(areaName, tx.getHash().serialize(), tx);
        }
        System.out.println("存入20万条用时：" + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 200000; i = i + 2) {
            dbService.getModel(areaName, txList.get(i).getHash().serialize());
        }
        System.out.println("查询10万次用时：" + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 200000; i = i + 2) {
            dbService.delete(areaName, txList.get(i).getHash().serialize());
        }
        System.out.println("删除10万次用时：" + (System.currentTimeMillis() - start) + "ms");
    }

}