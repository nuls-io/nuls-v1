package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class LockData extends BaseNulsData {

    private String address;

    private Na na;

    private long unlockTime;

    private int unlockHeight;

    private boolean canBeUnlocked;

    @Override
    public int size() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub(niels)

    }
}
