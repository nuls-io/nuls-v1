package io.nuls.core.chain.entity.transaction;

import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 2017/11/14.
 */
public abstract class NulsTransaction extends Transaction {
    private long lockTime;
    protected List<TransactionInput> inputs;
    protected List<TransactionOutput> outputs;

    public NulsTransaction(){
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.time = TimeService.currentTimeMillis();
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version);
        s += VarInt.sizeOf(type);
        s += getHash().getBytes().length;
        s += VarInt.sizeOf(time);
        s += VarInt.sizeOf(lockTime);
        s += remark.length;
        for (int i = 0; i < inputs.size(); i++) {
            s += inputs.get(i).size();
        }
        for (int i = 0; i < outputs.size(); i++) {
            s += outputs.get(i).size();
        }

        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version).encode());
        stream.write(new VarInt(type).encode());
        stream.write(hash.getBytes());
        stream.write(new VarInt(time).encode());
        stream.write(new VarInt(lockTime).encode());
        stream.write(remark);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        if(byteBuffer == null) {
            return;
        }
        version = (int)byteBuffer.readUint32();
        type = (int)byteBuffer.readUint32();

    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }
}
