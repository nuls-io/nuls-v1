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

package io.nuls.cache;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.model.CacheListenerItem;
import org.ehcache.spi.copy.Copier;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/6
 */
public class CacheMapTest {
    private CacheMap<String, ValueData> cacheMap;
    private NulsCacheListener<String, ValueData> listener;
    private Copier<ValueData> valueCopier;
    protected Map<String, Boolean> map = new HashMap<>();

    @Before
    public void before() {
        listener = new NulsCacheListener<String, ValueData>() {
            @Override
            public void onCreate(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_create", true);
                System.out.println("create");
            }

            @Override
            public void onEvict(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_evict", true);
                System.out.println("evict");


            }

            @Override
            public void onRemove(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_remove", true);
                System.out.println("remove");
            }

            @Override
            public void onUpdate(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_update", true);
                System.out.println("update");
            }

            @Override
            public void onExpire(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_expire", true);
                System.out.println("expire");
            }
        };
        valueCopier = new Copier<ValueData>() {
            @Override
            public ValueData copyForRead(ValueData valueData) {
//                return valueData.copy();
                return valueData;
            }

            @Override
            public ValueData copyForWrite(ValueData valueData) {
                return valueData.copy();
            }
        };
        this.cacheMap = new CacheMap("test-cache", 1, String.class, ValueData.class, 10, 10, listener, valueCopier);
    }

    @Test
    public void test() {
        ValueData data1 = new ValueData();
        data1.setTime(1000L);
        data1.setName("test1");
        data1.setCode(1);
        cacheMap.put(data1.getName(), data1);
        ValueData data_get = cacheMap.get(data1.getName());
        assertNotNull(data_get);
        assertNotEquals(data1, data_get);
        long start = System.currentTimeMillis();
        while (null == map.get(data1.getName() + "_create")) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("create late:" + (System.currentTimeMillis() - start));

        assertTrue(map.get(data1.getName() + "_create"));


        data1.setTime(10001L);
        cacheMap.put(data1.getName(), data1);
//        assertTrue(map.get(data1.getName() + "_update"));

        try {
            Thread.sleep(12000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertNull(cacheMap.get(data1.getName()));

        start = System.currentTimeMillis();
        while (null == map.get(data1.getName() + "_expire")) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("expire late:" + (System.currentTimeMillis() - start));
        assertTrue(map.get(data1.getName() + "_expire"));

        cacheMap.put(data1.getName(), data1);
        cacheMap.remove(data1.getName());
        start = System.currentTimeMillis();
        while (null == map.get(data1.getName() + "_remove")) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("remove late:" + (System.currentTimeMillis() - start));
        assertTrue(map.get(data1.getName() + "_remove"));


        for (int i = 0; i < 100000; i++) {
            ValueData data = new ValueData();
            data.setTime(1000L);
            data.setName("test" + i);
            data.setCode(i);
            cacheMap.put(data.getName(), data);
            System.out.println(i + "=====size=" + cacheMap.size());
        }


    }

    static class ValueData implements Serializable {
        private int code;
        private String name;
        private long time;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public ValueData copy() {
            ValueData data = new ValueData();
            data.setCode(code);
            data.setName(name);
            data.setTime(time);
            System.out.println("copy run...");
            return data;
        }
    }
}