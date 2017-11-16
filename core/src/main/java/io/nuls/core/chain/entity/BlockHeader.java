package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by win10 on 2017/10/30.
 */
public class BlockHeader extends NulsData{

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

    @Override
    public int size() {
        return 0;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
    }

    public Sha256Hash getHash() {
        return hash;
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
