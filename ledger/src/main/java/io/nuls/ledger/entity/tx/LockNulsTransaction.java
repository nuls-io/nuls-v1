package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class LockNulsTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {

    private long unlockTime;

    private int unlockHeight;

    private boolean canBeUnlocked;

    public LockNulsTransaction() {
        super(TransactionConstant.TX_TYPE_LOCK);
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo

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
