package io.nuls.utxo.accounts.model;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;

public class UtxoAccountsBalance extends BaseNulsData {
    private byte[] owner;
    private Na balance;
    private Na hadLocked;

    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public Na getBalance() {
        return balance;
    }

    public void setBalance(Na balance) {
        this.balance = balance;
    }

    public Na getHadLocked() {
        return hadLocked;
    }

    public void setHadLocked(Na hadLocked) {
        this.hadLocked = hadLocked;
    }

    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfBytes(owner);
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(owner);
        stream.writeInt64(balance.getValue());
        stream.writeInt64(hadLocked.getValue());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.owner = byteBuffer.readByLengthByte();
        this.balance = Na.valueOf(byteBuffer.readInt64());
        this.hadLocked = Na.valueOf(byteBuffer.readInt64());
    }
}
