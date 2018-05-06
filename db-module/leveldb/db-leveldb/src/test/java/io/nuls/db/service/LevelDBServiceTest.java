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

package io.nuls.db.service;

import io.nuls.db.entity.DBTestEntity;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.service.impl.LevelDBServiceImpl;
import io.nuls.kernel.model.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/6.
 */
public class LevelDBServiceTest {

    private DBService dbService;

    private String areaName = "transaction";

    @Before
    public void init() {
        dbService = new LevelDBServiceImpl();
        dbService.createArea(areaName);
    }

    @After
    public void clear() {
    }

    @Test
    public void testPerformanceTesting() {

        long time = System.currentTimeMillis();

        long maxCount = 1000000;
        for(long i = 0 ; i < maxCount ; i++) {
            DBTestEntity entity = new DBTestEntity();
            entity.setTime(i);

            byte[] key = entity.serializeForHash();
            byte[] value = entity.serialize();

            Result result = dbService.put(areaName, key, value);
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        System.out.println("Save " + maxCount + " transaction time-consuming : " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        long getCount = 10000;

        System.out.println("Test random performance of " + getCount + " data ···");
        for(long i = 0 ; i < getCount ; i++) {

            long index = (long) (Math.random() * maxCount);
            DBTestEntity entity = new DBTestEntity();
            entity.setTime(index);

            byte[] resultBytes = dbService.get(areaName, entity.serializeForHash());
            assertNotNull(resultBytes);

            DBTestEntity e = new DBTestEntity();
            e.parse(resultBytes);
            assertEquals(e.getTime(), index);
        }

        System.out.println("It takes " + (System.currentTimeMillis() - time) + " ms to randomly acquire " + getCount + " data");
    }
}