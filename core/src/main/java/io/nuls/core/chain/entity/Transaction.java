package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.validator.tx.TxMaxSizeValidator;
import io.nuls.core.validate.validator.tx.TxRemarkValidator;
import io.nuls.core.validate.validator.tx.TxSignValidator;
import io.nuls.core.validate.validator.tx.TxTypeValidator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Transaction extends BaseNulsData {
    public Transaction(int type) {
        this.time = TimeService.currentTimeMillis();
        this.registerValidator(new TxMaxSizeValidator());
        this.registerValidator(new TxRemarkValidator());
        this.registerValidator(new TxTypeValidator());
        this.registerValidator(new TxSignValidator());
        this.type = type;
    }

    //tx type
    protected final int type;
    //todo tx hash
    protected Sha256Hash hash;
    /**
     * current time (ms)
     *
     * @return
     */
    protected long time;
    protected byte[] remark;

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(type);
        size += VarInt.sizeOf(time);
        //todo the length of hash may change
        size += Sha256Hash.LENGTH;
        if (null != remark) {
            size += remark.length;
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo  hash may change
        stream.write(new VarInt(type).encode());
        stream.write(new VarInt(time).encode());
        stream.write(remark);

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo

    }

    public Sha256Hash getHash() {
        return hash;
    }

    public void setHash(Sha256Hash hash) {
        this.hash = hash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
    }
}
