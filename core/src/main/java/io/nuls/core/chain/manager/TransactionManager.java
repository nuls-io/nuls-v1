package io.nuls.core.chain.manager;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class TransactionManager {

    private static final Map<Integer, Class<? extends Transaction>> TX_MAP = new HashMap<>();
    private static final Map<Class<? extends Transaction>, TransactionService> TX_SERVICE_MAP = new HashMap<>();

    public static final void putTx(int txType, Class<? extends Transaction> txClass, TransactionService txService) {
        if (TX_MAP.containsKey(txType)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Transaction type repeating!");
        }
        TX_MAP.put(txType, txClass);
        TX_SERVICE_MAP.put(txClass, txService);
    }

    public static final Class<? extends Transaction> getTxClass(int txType) {
        return TX_MAP.get(txType);
    }

    public static List<Transaction> getInstances(NulsByteBuffer byteBuffer) throws InstantiationException, IllegalAccessException, NulsException {
        List<Transaction> list = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            list.add(getInstance(byteBuffer));
        }
        return list;
    }

    public static Transaction getInstance(NulsByteBuffer byteBuffer) throws IllegalAccessException, InstantiationException, NulsException {
        int txType = (int) new NulsByteBuffer(byteBuffer.getPayloadByCursor()).readVarInt();
        Class<? extends Transaction> txClass = getTxClass(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "transaction type not exist!");
        }
        Transaction tx = byteBuffer.readNulsData(txClass.newInstance());
        return tx;
    }

    public void onRollback(Transaction tx) {

    }

    public void onCommit(Transaction tx) {

    }

    public void onApproval(Transaction tx) {

    }

    public static TransactionService getService(Class<? extends Transaction> txClass) {
        return TX_SERVICE_MAP.get(txClass);
    }
}
