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
package io.nuls.contract.vm.program.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.nuls.db.service.DBService;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;

import java.util.concurrent.TimeUnit;

public class KeyValueSource implements Source<byte[], byte[]> {

    public static final String AREA = "contract";

    private DBService dbService;

    private final Cache<ByteArrayWrapper, byte[]> cache;

    public KeyValueSource(DBService dbService) {
        this.dbService = dbService;
        String[] areas = dbService.listArea();
        if (!ArrayUtils.contains(areas, AREA)) {
            dbService.createArea(AREA);
        }
        this.cache = CacheBuilder.newBuilder()
                .initialCapacity(102400)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void put(byte[] key, byte[] val) {
        cache.put(new ByteArrayWrapper(key), val);
        dbService.put(AREA, key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        byte[] bytes = cache.getIfPresent(new ByteArrayWrapper(key));
        if (bytes == null) {
            bytes = dbService.get(AREA, key);
        }
        return bytes;
    }

    @Override
    public void delete(byte[] key) {
    }

    @Override
    public boolean flush() {
        return true;
    }

}
