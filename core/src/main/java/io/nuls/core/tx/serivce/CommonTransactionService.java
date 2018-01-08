package io.nuls.core.tx.serivce;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.validate.ValidateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * tx oprations
 *
 * @author Niels
 * @date 2018/1/8
 */
public class CommonTransactionService {

    public ValidateResult verify(Transaction tx) {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        return tx.verify();
    }

    public void rollback(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onRollback(tx);
        }
    }

    public void commit(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onCommit(tx);
        }
    }

    public void approval(Transaction tx) throws NulsException {
        AssertUtil.canNotEmpty(tx, ErrorCode.NULL_PARAMETER);
        List<TransactionService> serviceList = getServiceList(tx.getClass());
        for (TransactionService service : serviceList) {
            service.onApproval(tx);
        }
    }

    public List<TransactionService> getServiceList(Class<? extends Transaction> txClass) {
        List<TransactionService> list = new ArrayList<>();
        Class clazz = txClass;
        while (!clazz.equals(Transaction.class)) {
            TransactionService txService = TransactionManager.getService(clazz);
            if (null != txService) {
                list.add(0, txService);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }
}
