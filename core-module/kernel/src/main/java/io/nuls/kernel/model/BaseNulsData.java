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
package io.nuls.kernel.model;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsVerificationException;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.kernel.validate.ValidatorManager;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class BaseNulsData implements NulsData, Serializable, Cloneable {

    protected transient static Map<Class<? extends BaseNulsData>, RuntimeSchema<? extends BaseNulsData>> SCHEMA_MAP = new ConcurrentHashMap<>();

    protected transient NulsDataType dataType;

    public BaseNulsData() {
        if (SCHEMA_MAP.get(this.getClass()) == null) {
            RuntimeSchema<? extends BaseNulsData> schema = RuntimeSchema.createFrom(this.getClass());
            SCHEMA_MAP.put(this.getClass(), schema);
        }
    }

    public final int size() {
        return this.serialize().length;
    }

    /**
     * First, serialize the version field
     */
    public final byte[] serialize() {
        RuntimeSchema schema = SCHEMA_MAP.get(this.getClass());
        return ProtostuffIOUtil.toByteArray(this, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    public final void parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        RuntimeSchema schema = SCHEMA_MAP.get(this.getClass());
        ProtostuffIOUtil.mergeFrom(bytes, this, schema);
    }

    protected void registerValidator(NulsDataValidator<? extends BaseNulsData> validator) {
        ValidatorManager.addValidator(this.getClass(), validator);
    }

    /**
     * @throws NulsException
     */
    public final ValidateResult verify() {
        ValidateResult result = ValidatorManager.startDoValidator(this);
        return result;
    }

    public final void verifyWithException() throws NulsVerificationException {
        ValidateResult result = this.verify();
        if (result.isFailed()) {
            throw new NulsVerificationException(result.getMessage());
        }
    }

    public NulsDataType getDataType() {
        return dataType;
    }

    public void setDataType(NulsDataType dataType) {
        this.dataType = dataType;
    }


}
