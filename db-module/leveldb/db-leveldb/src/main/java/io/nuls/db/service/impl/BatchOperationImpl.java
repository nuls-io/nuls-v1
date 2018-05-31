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
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.model.Result;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/9
 */
public class BatchOperationImpl implements BatchOperation {

    private static final Result FAILED_NULL = Result.getFailed(DBErrorCode.NULL_PARAMETER);
    private static final Result SUCCESS = Result.getSuccess();
    private static final Result FAILED_BATCH_CLOSE = Result.getFailed(DBErrorCode.DB_BATCH_CLOSE);
    private String area;
    private DB db;
    private WriteBatch batch;
    private volatile boolean isClose = false;

    BatchOperationImpl(String area) {
        this.area = area;
        db = LevelDBManager.getArea(area);
        if(db != null) {
            batch = db.createWriteBatch();
        }
    }

    public Result checkBatch() {
        if(db == null) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if(batch == null) {
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
        return SUCCESS;
    }

    /**
     * @param key
     * @param value
     * @return
     */
    @Override
    public Result put(byte[] key, byte[] value) {
        if(key == null || value == null) {
            return FAILED_NULL;
        }
        batch.put(key, value);
        return SUCCESS;
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
            return FAILED_NULL;
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
            return FAILED_NULL;
        }
        batch.delete(key);
        return SUCCESS;
    }

    private void close() {
        this.isClose = true;
    }

    private boolean checkClose() {
        return isClose;
    }

    /**
     * @return
     */
    @Override
    public Result executeBatch() {
        // 检查逻辑关闭
        if(checkClose()) {
            return FAILED_BATCH_CLOSE;
        }
        try {
            db.write(batch);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION, e.getMessage());
        } finally {
            // Make sure you close the batch to avoid resource leaks.
            // 貌似LevelDB未实现此close方法, 所以加入一个逻辑关闭
            if(batch != null) {
                try {
                    this.close();
                    batch.close();
                } catch (IOException e) {
                    // skip it
                }
            }
        }
        return SUCCESS;
    }
}
