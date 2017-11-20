package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/20.
 */
public class LockCoinTransaction<T extends NulsData> extends CoinTransaction<T> {

    private long unlockTime;

    private int unlockHeight;

    private boolean canBeUnlocked;

    public LockCoinTransaction() {
        this.type = TransactionConstant.TX_TYPE_LOCK;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
    }

    public int getUnlockHeight() {
        return unlockHeight;
    }

    public void setUnlockHeight(int unlockHeight) {
        this.unlockHeight = unlockHeight;
    }

    public boolean isCanBeUnlocked() {
        return canBeUnlocked;
    }

    public void setCanBeUnlocked(boolean canBeUnlocked) {
        this.canBeUnlocked = canBeUnlocked;
    }
}
