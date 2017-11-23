package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.validate.validator.tx.TxMaxSizeValidator;
import io.nuls.core.validate.validator.tx.TxRemarkValidator;
import io.nuls.core.validate.validator.tx.TxSignValidator;
import io.nuls.core.validate.validator.tx.TxTypeValidator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Transaction extends BaseNulsData {
    public Transaction() {
        this.time = TimeService.currentTimeMillis();
        this.registerValidator(new TxMaxSizeValidator());
        this.registerValidator(new TxRemarkValidator());
        this.registerValidator(new TxTypeValidator());
        this.registerValidator(new TxSignValidator());
    }

    //tx type
    protected int type;
    //tx hash
    protected Sha256Hash hash;
    //current time (ms)
    protected long time;
    protected byte[] remark;

    @Override
    public int size() {
        int size = 0;
        //size += version;
        size += VarInt.sizeOf(type);
        //todo

        return size;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        //todo

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

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
    }
}
