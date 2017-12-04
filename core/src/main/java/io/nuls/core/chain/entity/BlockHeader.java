package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/10/30
 */
public class BlockHeader extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    //区块hash
    protected Sha256Hash hash;
    //上一区块hash
    protected Sha256Hash preHash;
    //梅克尔树根节点hash
    protected Sha256Hash merkleHash;
    //时间戳，单位（秒）
    protected long time;
    //区块高度
    protected long height;
    //交易数
    protected long txCount;
    //该时段共识人数
    protected int periodCount;
    //本轮开始的时间点，单位（秒）
    protected long periodStartTime;
    //时段，一轮共识中的第几个时间段，可验证对应的共识人
    protected int timePeriod;
    //签名脚本，包含共识打包人信息和签名，签名是对以上信息的签名
    protected byte[] scriptBytes;

    protected Script scriptSig;

    public BlockHeader() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public BlockHeader(long height, long time) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.height = height;
        this.time = time;
    }

    public BlockHeader(long height, long time, Sha256Hash preHash) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.height = height;
        this.time = time;
        this.preHash = preHash;
    }

    @Override
    protected int dataSize() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        //todo
    }

    public Sha256Hash getHash() throws IOException {
        if (hash == null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Utils.uint32ToByteStreamLE(version.getVersion(), stream);
                stream.write(preHash.getBytes());
//                stream.write(merkleHash.getBytes());
                Utils.int64ToByteStreamLE(time, stream);
                Utils.uint32ToByteStreamLE(height, stream);
                stream.write(new VarInt(periodCount).encode());
                stream.write(new VarInt(timePeriod).encode());
                Utils.uint32ToByteStreamLE(periodStartTime, stream);
                //交易数量
                stream.write(new VarInt(txCount).encode());
                hash = Sha256Hash.twiceOf(stream.toByteArray());
            } finally {
                stream.close();
            }
        }
        return hash;
    }

    public static void main(String[] args) throws IOException {
        Block block = new Block(1,1,Sha256Hash.twiceOf("0000".getBytes()));
        System.out.println(Hex.encode(block.getHash().getBytes()).length() );;
    }

    public void setHash(Sha256Hash hash) {
        this.hash = hash;
    }

    public Sha256Hash getPreHash() {
        return preHash;
    }

    public void setPreHash(Sha256Hash preHash) {
        this.preHash = preHash;
    }

    public Sha256Hash getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(Sha256Hash merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(int periodCount) {
        this.periodCount = periodCount;
    }

    public long getPeriodStartTime() {
        return periodStartTime;
    }

    public void setPeriodStartTime(long periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    public void setScriptBytes(byte[] scriptBytes) {
        this.scriptBytes = scriptBytes;
    }

    public Script getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(Script scriptSig) {
        this.scriptSig = scriptSig;
    }

}
