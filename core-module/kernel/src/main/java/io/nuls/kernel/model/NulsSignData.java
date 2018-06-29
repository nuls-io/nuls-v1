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

import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author facjas
 */
public class NulsSignData extends BaseNulsData {

    public static byte SIGN_ALG_ECC = (short) 0;
    public static byte SIGN_ALG_DEFAULT = NulsSignData.SIGN_ALG_ECC;
    /**
     * 算法类型
     */
    protected byte signAlgType;

    /**
     * 签名字节组
     */
    protected byte[] signBytes;

    public byte getSignAlgType() {
        return signAlgType;
    }

    public void setSignAlgType(byte signAlgType) {
        this.signAlgType = signAlgType;
    }

    public NulsSignData() {
    }

    @Override
    public int size() {
        return SerializeUtils.sizeOfBytes(signBytes) + 1;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(signAlgType);
        stream.writeBytesWithLength(signBytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.signAlgType = byteBuffer.readByte();
        this.signBytes = byteBuffer.readByLengthByte();
    }


    public byte[] getSignBytes() {
        return signBytes;
    }

    public void setSignBytes(byte[] signBytes) {
        this.signBytes = signBytes;
    }


    public NulsSignData sign(NulsDigestData nulsDigestData, short signAlgType, BigInteger privkey) {
        if (signAlgType == NulsSignData.SIGN_ALG_ECC) {
            ECKey ecKey = ECKey.fromPrivate(privkey);
            byte[] signBytes = ecKey.sign(nulsDigestData.getDigestBytes(), privkey);
            NulsSignData signData = new NulsSignData();
            try {
                signData.parse(signBytes,0);
            } catch (NulsException e) {
                Log.error(e);
            }
            return signData;
        }
        return null;
    }

    public NulsSignData sign(NulsDigestData nulsDigestData, BigInteger privkey) {
        short signAlgType = NulsSignData.SIGN_ALG_DEFAULT;
        if (signAlgType == NulsSignData.SIGN_ALG_ECC) {
            ECKey ecKey = ECKey.fromPrivate(privkey);
            return sign(nulsDigestData, signAlgType, privkey);
        }
        return null;
    }

    @Override
    public String toString() {
        try {
            return Hex.encode(serialize());
        } catch (IOException e) {
            Log.error(e);
            return super.toString();
        }
    }
}
