/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.core.tools.crypto.UnsafeByteArrayOutputStream;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.exception.NulsVerificationException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.kernel.validate.ValidatorManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Niels
 */
public abstract class BaseNulsData implements NulsData, Serializable, Cloneable {

    @Override
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
                throw new NulsRuntimeException(KernelErrorCode.SERIALIZE_ERROR);
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

    protected abstract void serializeToStream(NulsOutputStreamBuffer stream) throws IOException;


    @Override
    public final void parse(byte[] bytes, int cursor) throws NulsException {
        if (bytes == null || bytes.length == 0 || ((bytes.length == 4) && Arrays.equals(NulsConstant.PLACE_HOLDER, bytes))) {
            return;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        byteBuffer.setCursor(cursor);
        this.parse(byteBuffer);
    }

    public abstract void parse(NulsByteBuffer byteBuffer) throws NulsException;

    public final ValidateResult verify() {
        ValidateResult result = ValidatorManager.startDoValidator(this);
        return result;
    }

    public final void verifyWithException() throws NulsVerificationException {
        ValidateResult result = this.verify();
        if (result.isFailed()) {
            throw new NulsVerificationException(result.getErrorCode());
        }
    }

}
