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

import com.google.common.collect.Maps;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.Result;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/9
 */
public class BatchOperationImpl implements BatchOperation {

    private String area;

    BatchOperationImpl(String area) {
        this.area = area;
    }

    private final List<Entry<byte[], byte[]>> batchPut = new ArrayList<>();
    private final List<byte[]> batchDelete = new ArrayList<>();

    /**
     * @param key
     * @param value
     * @return
     */
    @Override
    public Result put(byte[] key, byte[] value) {
        if(key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        batchPut.add(Maps.immutableEntry(key, value));
        return Result.getSuccess();
    }

    /**
     * @param area
     * @param key
     * @param value 需要存储或者更新的对象/Objects that need to be added or updated.
     * @param <T>
     * @return
     */
    @Override
    public <T> Result putModel(byte[] key, T value) {
        if(key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] bytes = LevelDBManager.getModelSerialize(value);
        return put(key, bytes);
    }

    /**
     * @param key
     * @return
     */
    @Override
    public Result delete(byte[] key) {
        if(key == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        batchDelete.add(key);
        return Result.getSuccess();
    }

    /**
     * @return
     */
    @Override
    public Result executeBatch() {
        DB db = LevelDBManager.getArea(area);
        if(db == null) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        WriteBatch batch = null;
        try {
            batch = db.createWriteBatch();
            for(Entry<byte[], byte[]> entry : batchPut) {
                batch.put(entry.getKey(), entry.getValue());
            }
            for(byte[] key : batchDelete) {
                batch.delete(key);
            }
            db.write(batch);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.DB_UNKOWN_EXCEPTION, e.getMessage());
        } finally {
            // Make sure you close the batch to avoid resource leaks.
            // 貌似LevelDB未实现此close方法
            if(batch != null) {
                try {
                    batch.close();
                } catch (IOException e) {
                    // skip it
                }
            }
            batchPut.clear();
            batchDelete.clear();
        }
        return Result.getSuccess();
    }
}
