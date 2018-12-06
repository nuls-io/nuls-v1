/**
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
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
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Result;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

public class BatchOperationImpl implements BatchOperation {


    static {
        RocksDB.loadLibrary();
    }

    private static final Result FAILED_NULL = Result.getFailed(DBErrorCode.NULL_PARAMETER);
    private static final Result SUCCESS = Result.getSuccess();
    private static final Result FAILED_BATCH_CLOSE = Result.getFailed(DBErrorCode.DB_BATCH_CLOSE);
    private String area;
    private RocksDB db;
    private WriteBatch batch;
    private volatile boolean isClose = false;

    BatchOperationImpl(String area) {
        this.area = area;
        db = LevelDBManager.getArea(area);
        if (db != null) {
            batch = new WriteBatch();
        }
    }

    public Result checkBatch() {
        if (db == null) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (batch == null) {
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
        return SUCCESS;
    }

    @Override
    public Result put(byte[] key, byte[] value) {
        if (key == null || value == null) {
            return FAILED_NULL;
        }
        try {
            batch.put(key, value);
        } catch (RocksDBException e) {
            Log.error(e);
            return Result.getFailed();
        }
        return SUCCESS;
    }

    @Override
    public <T> Result putModel(byte[] key, T value) {
        if (key == null || value == null) {
            return FAILED_NULL;
        }
        byte[] bytes = LevelDBManager.getModelSerialize(value);
        return put(key, bytes);
    }

    @Override
    public Result delete(byte[] key) {
        if (key == null) {
            return FAILED_NULL;
        }
        try {
            batch.delete(key);
        } catch (RocksDBException e) {
            Log.error(e);
            return Result.getFailed();
        }
        return SUCCESS;
    }

    private void close() {
        this.isClose = true;
    }

    private boolean checkClose() {
        return isClose;
    }

    @Override
    public Result executeBatch() {
        // 检查逻辑关闭
        if (checkClose()) {
            throw new NulsRuntimeException(DBErrorCode.DB_AREA_FAILED_BATCH_CLOSE);
        }
        try {
            db.write(new WriteOptions(), batch);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(DBErrorCode.DB_UNKOWN_EXCEPTION);
        } finally {
            // Make sure you close the batch to avoid resource leaks.
            // 关闭批量操作对象释放资源
            if (batch != null) {
                this.close();
                batch.close();
            }
        }
        return Result.getSuccess();
    }
}
