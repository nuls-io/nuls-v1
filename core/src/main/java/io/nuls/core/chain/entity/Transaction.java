package io.nuls.core.chain.entity;

import io.nuls.core.chain.manager.TransactionValidatorManager;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.NulsDataValidator;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Transaction extends BaseNulsData {

    private TransactionListener listener;
    /**
     * tx type
     */
    private int type;
    private NulsDigestData hash;
    private NulsSignData sign;
    /**
     * current time (ms)
     *
     * @return
     */
    protected long time;
    protected byte[] remark;

    public final void onRollback() {
        if (null != listener) {
            listener.onRollback(this);
        }
    }

    public final void onCommit() {
        if (null != listener) {
            listener.onCommit(this);
        }
    }

    public final void onApproval() {
        if (null != listener) {
            listener.onApproval(this);
        }
    }

    public Transaction(int type) {
        this.dataType = NulsDataType.TRANSACTION;
        this.time = TimeService.currentTimeMillis();
        this.type = type;
        this.initValidators();
    }

    public Transaction(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    private void initValidators() {
        List<NulsDataValidator> list = TransactionValidatorManager.getValidators();
        for (NulsDataValidator<Transaction> validator : list) {
            this.registerValidator(validator);
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(type);
        size += VarInt.sizeOf(time);
        size += hash.size();
        size += sign.size();
        if (null != remark) {
            size += remark.length;
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(type);
        stream.writeVarInt(time);
        stream.write(hash.serialize());
        stream.write(sign.serialize());
        stream.writeBytesWithLength(remark);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        type = (int) byteBuffer.readVarInt();
        time = byteBuffer.readVarInt();

        hash = new NulsDigestData();
        hash.parse(byteBuffer);

        sign = new NulsSignData();
        sign.parse(byteBuffer);

        this.remark = byteBuffer.readByLengthByte();
    }

    public void registerListener(TransactionListener listener) {
        this.listener = listener;
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

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }
}
