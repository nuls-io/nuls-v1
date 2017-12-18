package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * Created by win10 on 2017/10/30.
 */
public class UtxoOutput extends BaseNulsData{
    //交易
    private NulsDigestData txHash;
    //下次的花费
    private UtxoInput spentBy;
    //交易金额
    private long value;
    //锁定时间
    private long lockTime;

    private byte[] scriptBytes;

    private Script script;
    //交易输出的索引
    private int index;

    public UtxoOutput() {

    }

    public UtxoOutput(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(value);
        s += VarInt.sizeOf(lockTime);
        s += scriptBytes.length;
        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(value);
        stream.writeInt32((int) lockTime);
        stream.write(new VarInt(scriptBytes.length).encode());
        stream.write(scriptBytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        if(byteBuffer == null ) {
            return;
        }
        value = byteBuffer.readInt64();
        lockTime = byteBuffer.readUint32();
        //赎回脚本名的长度
        int signLength = (int)byteBuffer.readVarInt();
        scriptBytes = byteBuffer.readBytes(signLength);
        script = new Script(scriptBytes);
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public UtxoInput getSpentBy() {
        return spentBy;
    }

    public void setSpentBy(UtxoInput spentBy) {
        this.spentBy = spentBy;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    public void setScriptBytes(byte[] scriptBytes) {
        this.scriptBytes = scriptBytes;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


}
