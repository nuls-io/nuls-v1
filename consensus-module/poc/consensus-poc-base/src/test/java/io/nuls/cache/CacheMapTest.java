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
    private CacheMap cacheMap;
    private NulsCacheListener<String, ValueData> listener;
    private Copier<ValueData> valueCopier;
    protected Map<String, Boolean> map = new HashMap<>();

    @Before
    public void before() {
        listener = new NulsCacheListener<String, ValueData>() {
            @Override
            public void onCreate(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_create", true);
            }

            @Override
            public void onEvict(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_evict", true);

            }

            @Override
            public void onRemove(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_remove", true);
            }

            @Override
            public void onUpdate(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_update", true);
            }

            @Override
            public void onExpire(CacheListenerItem<String, ValueData> item) {
                map.put(item.getKey() + "_expire", true);
            }
        };
        valueCopier = new Copier<ValueData>() {
            @Override
            public ValueData copyForRead(ValueData valueData) {
                return valueData.copy();
            }

            @Override
            public ValueData copyForWrite(ValueData valueData) {
                return valueData.copy();
            }
        };
        this.cacheMap = new CacheMap("test-cache", 1, 60, 60, listener, valueCopier);
    }

    @Test
    public void testPut() {
        ValueData data1 = new ValueData();
        data1.setTime(1000L);
        data1.setName("test1");
        data1.setCode(1);
        cacheMap.put(data1.getName(), data1);

        assertNotNull(cacheMap.get(data1.getName()));

        try {
            Thread.sleep(600000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertNull(cacheMap.get(data1.getName()));


    }

    static class ValueData implements Serializable{
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