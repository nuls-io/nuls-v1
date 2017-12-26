package io.nuls.core.chain.entity;


import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
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

    public static final NulsDigestData EMPTY_HASH = new NulsDigestData(new byte[]{0});
    protected int digestAlgType;
    protected int digestLength;
    protected byte[] digestBytes;

    public NulsDigestData() {
    }

    public NulsDigestData(byte[] bytes) {
        this();
        this.digestBytes = bytes;
    }

    public int getDigestAlgType() {
        return digestAlgType;
    }

    public void setDigestAlgType(int digestAlgType) {
        this.digestAlgType = digestAlgType;
    }

    @Override
    public int size() {
        return digestBytes.length + 2;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.write(digestAlgType);
        buffer.write(digestLength);
        buffer.write(digestBytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setDigestAlgType(byteBuffer.readByte());
        this.setDigestLength(byteBuffer.readByte());
        this.setDigestBytes(byteBuffer.readBytes(this.digestLength));
    }

    public int getDigestLength() {
        return digestLength;
    }

    public void setDigestLength(int digestLength) {
        this.digestLength = digestLength;
    }

    public byte[] getDigestBytes() {
        return this.digestBytes;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[digestLength + 2];
        bytes[0] = (byte) digestAlgType;
        bytes[1] = (byte) digestLength;
        System.arraycopy(digestBytes, 0, bytes, 2, digestLength);
        return bytes;
    }

    public String getDigestHex() {
        return Hex.encode(toBytes());
    }

    private void setDigestBytes(byte[] digestBytes) {
        this.digestBytes = digestBytes;
    }

    public static NulsDigestData calcDigestData(BaseNulsData data) {
        return calcDigestData(data, 0);
    }

    public static NulsDigestData calcDigestData(BaseNulsData data, int digestAlgType) {
        try {
            return calcDigestData(data.serialize(), digestAlgType);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static NulsDigestData calcDigestData(byte[] data) {
        return calcDigestData(data, 0);
    }

    public static NulsDigestData calcDigestData(byte[] data, int digestAlgType) {
        NulsDigestData digestData = new NulsDigestData();
        digestData.setDigestAlgType(digestAlgType);
        byte[] content = Sha256Hash.hashTwice(data);
        digestData.setDigestLength(content.length);
        digestData.setDigestBytes(content);
        return digestData;
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
        Sha256Hash merkleHash = Sha256Hash.wrap(ddList.get(ddList.size() - 1).getDigestBytes());
        return new NulsDigestData(merkleHash.getBytes());
    }

    public static void main(String[] args) {
        NulsTextData textData = new NulsTextData();
        textData.setText("this is a text");
        NulsDigestData nulsDigestData = NulsDigestData.calcDigestData(textData);
        System.out.println(nulsDigestData.getDigestHex());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NulsDigestData)) {
            return false;
        }
        if (this.getDigestBytes() == null || ((NulsDigestData) obj).getDigestBytes() == null) {
            return false;
        }
        if (this.getDigestBytes().length != ((NulsDigestData) obj).getDigestBytes().length) {
            return false;
        }
        return this.getDigestHex().equals(((NulsDigestData) obj).getDigestHex());
    }
}
