package io.nuls.protocol.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;

public class TransactionPoTest extends Transaction {
    public TransactionPoTest(int type) {
        super(type);
    }

    @Override
    protected TransactionLogicData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public String getInfo(byte[] address) {
        return null;
    }
}
