package io.nuls.protocol.model.tx;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;

public class ProtocolTransaction extends Transaction {

    public ProtocolTransaction() {
        super(NulsConstant.TX_TYPE_PROTOCOL);
    }

    public ProtocolTransaction(int type) {
        super(type);
    }

    @Override
    protected TransactionLogicData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub
        return null;
    }

    @Override
    public String getInfo(byte[] address) {
        // todo auto-generated method stub
        return null;
    }
}
