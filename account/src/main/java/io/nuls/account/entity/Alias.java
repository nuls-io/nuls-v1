/**
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
 */
package io.nuls.account.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @date 2017/12/21.
 */
public class Alias extends BaseNulsData {

    private String address;

    private String alias;

    // 0: locked   1:confirm
    private int status;

    public static final int LOCKED = 0;
    public static final int CONFIRM = 1;
    public Alias() {

    }

    public Alias(String address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public Alias(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    @Override
    public int size() {
        int s = 0;
        try {
            s += address.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeString(alias);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            this.address = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
            this.alias = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
