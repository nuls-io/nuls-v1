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
package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.DataValidatorChain;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.core.validate.ValidatorManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class BaseNulsData implements Serializable, Cloneable {

    protected NulsDataType dataType;

    public BaseNulsData() {
    }

    public BaseNulsData(NulsByteBuffer buffer) throws NulsException {
        this.parse(buffer);
    }


    protected void registerValidator(NulsDataValidator<? extends BaseNulsData> validator) {
        ValidatorManager.addValidator(this.getClass(), validator);
    }

    public abstract int size();

    /**
     * First, serialize the version field
     *
     * @return
     */
    public final byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            int size = size();
            bos = new UnsafeByteArrayOutputStream(size);
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            if (size == 0) {
                bos.write(NulsConstant.PLACE_HOLDER);
            } else {
                serializeToStream(buffer);
            }
            byte[] bytes = bos.toByteArray();
            if (bytes.length != this.size()) {
                throw new NulsRuntimeException(ErrorCode.FAILED, "序列化和size长度不一致：" + this.getClass());
            }
            return bytes;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }

    public final void parse(byte[] bytes) throws NulsException {
        if (bytes == null || bytes.length == 0 || ((bytes.length == 4) && Arrays.equals(NulsConstant.PLACE_HOLDER, bytes))) {
            return;
        }
        this.parse(new NulsByteBuffer(bytes));
    }

    /**
     * serialize important field
     *
     * @param stream
     * @throws IOException
     */
    protected abstract void serializeToStream(NulsOutputStreamBuffer stream) throws IOException;

    protected abstract void parse(NulsByteBuffer byteBuffer) throws NulsException;

    /**
     * @throws NulsException
     */
    public final ValidateResult verify() {
        return ValidatorManager.startDoValidator(this);
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
