/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/4/18
 */
@Service
public class LevelDBServiceImpl implements DBService {

    public LevelDBServiceImpl() {
        try {
            LevelDBManager.init();
        } catch (Exception e) {
            //skip it
        }
    }

    @Override
    public Result createArea(String areaName) {
        return LevelDBManager.createArea(areaName);
    }

    @Override
    public Result createArea(String areaName, Long cacheSize) {
        return LevelDBManager.createArea(areaName, cacheSize);
    }

    @Override
    public Result createArea(String areaName, Comparator<byte[]> comparator) {
        return LevelDBManager.createArea(areaName, comparator);
    }

    @Override
    public Result createArea(String areaName, Long cacheSize, Comparator<byte[]> comparator) {
        return LevelDBManager.createArea(areaName, cacheSize, comparator);
    }

    @Override
    public String[] listArea() {
        return LevelDBManager.listArea();
    }

    @Override
    public Result put(String area, byte[] key, byte[] value) {
        return LevelDBManager.put(area, key, value);
    }

    @Override
    public <T> Result putModel(String area, byte[] key, T value) {
        return LevelDBManager.putModel(area, key, value);
    }

    @Override
    public Result delete(String area, byte[] key) {
        return LevelDBManager.delete(area, key);
    }

    @Override
    public byte[] get(String area, byte[] key) {
        return LevelDBManager.get(area, key);
    }

    @Override
    public <T> T getModel(String area, byte[] key, Class<T> clazz) {
        return LevelDBManager.getModel(area, key, clazz);
    }

    @Override
    public Object getModel(String area, byte[] key) {
        return LevelDBManager.getModel(area, key);
    }

    @Override
    public Set<byte[]> keySet(String area) {
        return LevelDBManager.keySet(area);
    }

    @Override
    public List<byte[]> keyList(String area) {
        return LevelDBManager.keyList(area);
    }

    @Override
    public Set<Entry<byte[], byte[]>> entrySet(String area) {
        return LevelDBManager.entrySet(area);
    }

    @Override
    public List<Entry<byte[], byte[]>> entryList(String area) {
        return LevelDBManager.entryList(area);
    }

    @Override
    public <T> List<Entry<byte[], T>> entryList(String area, Class<T> clazz) {
        return LevelDBManager.entryList(area, clazz);
    }

    @Override
    public <T> List<T> values(String area, Class<T> clazz) {
        return LevelDBManager.values(area, clazz);
    }

    @Override
    public BatchOperation createWriteBatch(String area) {
        if(StringUtils.isBlank(area)) {
            return null;
        }
        BatchOperationImpl batchOperation = new BatchOperationImpl(area);
        Result result = batchOperation.checkBatch();
        if(result.isFailed()) {
            Log.error("DB batch create error: " + result.getMsg());
            return null;
        }
        return batchOperation;
    }
}
