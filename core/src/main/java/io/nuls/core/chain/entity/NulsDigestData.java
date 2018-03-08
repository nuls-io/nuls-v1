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
package io.nuls.core.chain.entity;


import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/20
 */
public class NulsDigestData extends BaseNulsData {

    public static final NulsDigestData EMPTY_HASH = new NulsDigestData(new byte[]{0, 0,1, 0});
    protected short digestAlgType = DIGEST_ALG_SHA256;
    protected byte[] digestBytes;

    public static short DIGEST_ALG_SHA256=0;
    public static short DIGEST_ALG_SHA160=1;

    public NulsDigestData() {
    }

    public NulsDigestData(byte[] bytes) {
        this();
        try {
            this.parse(bytes);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    public short getDigestAlgType() {
        return digestAlgType;
    }

    public void setDigestAlgType(short digestAlgType) {
        this.digestAlgType = digestAlgType;
    }

    @Override
    public int size() {
        return Utils.sizeOfSerialize(digestBytes) + 2;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.writeShort(digestAlgType);
        buffer.writeBytesWithLength(digestBytes);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setDigestAlgType(byteBuffer.readShort());
        this.digestBytes = byteBuffer.readByLengthByte();
    }

    public String getDigestHex() {
        try {
            return Hex.encode(serialize());
        } catch (IOException e) {
            Log.error(e);
            return null;
        }
    }

    public static NulsDigestData fromDigestHex(String hex) {
        byte[] bytes = Hex.decode(hex);
        return new NulsDigestData(bytes);
    }

    public static NulsDigestData calcDigestData(BaseNulsData data) {
        return calcDigestData(data, (short) 0);
    }

    public static NulsDigestData calcDigestData(BaseNulsData data, short digestAlgType) {
        try {
            return calcDigestData(data.serialize(), digestAlgType);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public byte[] getDigestBytes() {
        return digestBytes;
    }

    public byte[] getWholeBytes() {
        if(null==digestBytes){
            return null;
        }
        byte[] bytes = new byte[2 + digestBytes.length];
        byte[] algBytes = Utils.shortToBytes(this.digestAlgType);
        System.arraycopy(algBytes, 0, bytes, 0, algBytes.length);
        System.arraycopy(digestBytes, 0, bytes, algBytes.length, digestBytes.length);
        return bytes;
    }

    public static NulsDigestData calcDigestData(byte[] data) {
        return calcDigestData(data, (short) 0);
    }

    public static NulsDigestData calcDigestData(byte[] data, short digestAlgType) {
        NulsDigestData digestData = new NulsDigestData();
        digestData.setDigestAlgType(digestAlgType);
        if((short)0 == digestAlgType) {
            byte[] content = Sha256Hash.hashTwice(data);
            digestData.digestBytes = content;
            return digestData;
        }
        //todo extend other algType

        if((short)1 == digestAlgType){
            byte[] content = Utils.sha256hash160(data);
            return digestData;
        }

        return null;
    }

    public static NulsDigestData calcMerkleDigestData(List<NulsDigestData> ddList) {
        //todo
        int levelOffset = 0;
        for (int levelSize = ddList.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            for (int left = 0; left < levelSize; left += 2) {
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = Utils.reverseBytes(ddList.get(levelOffset + left).getDigestBytes());
                byte[] rightBytes = Utils.reverseBytes(ddList.get(levelOffset + right).getDigestBytes());
                ddList.add(new NulsDigestData(Utils.reverseBytes(Sha256Hash.hashTwice(leftBytes, 0, 32, rightBytes, 0, 32))));
            }
            levelOffset += levelSize;
        }
        byte[] bytes = ddList.get(ddList.size() - 1).getDigestBytes();
        Sha256Hash merkleHash = Sha256Hash.wrap(bytes);
        NulsDigestData digestData = new NulsDigestData();
        digestData.digestBytes = merkleHash.getBytes();
        return digestData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NulsDigestData)) {
            return false;
        }
        if (this.getWholeBytes() == null || ((NulsDigestData) obj).getWholeBytes() == null) {
            return false;
        }
        if (this.getWholeBytes().length != ((NulsDigestData) obj).getWholeBytes().length) {
            return false;
        }
        return this.getDigestHex().equals(((NulsDigestData) obj).getDigestHex());
    }

    @Override
    public String toString() {
        return getDigestHex();
    }
}
