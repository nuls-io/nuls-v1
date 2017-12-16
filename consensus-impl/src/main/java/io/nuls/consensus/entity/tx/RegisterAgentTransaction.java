package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Delegate;
import io.nuls.consensus.entity.validator.consensus.AccountCreditValidator;
import io.nuls.consensus.entity.validator.consensus.AgentDepositValidator;
import io.nuls.consensus.tx.AbstractConsensusTransaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends AbstractConsensusTransaction<Delegate> {

    private LockNulsTransaction lockNulsTransaction;

    public RegisterAgentTransaction() {
        super(PocConsensusConstant.TX_TYPE_REGISTER_AGENT);
        this.registerValidator(new AccountCreditValidator());
        this.registerValidator(new AgentDepositValidator());
    }

    @Override
    protected Delegate parseBody(NulsByteBuffer byteBuffer) {
        Delegate delegate = new Delegate();
        delegate.parse(byteBuffer);
        return delegate;
    }

    @Override
    public int size() {
        if (null == lockNulsTransaction) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return super.size() + lockNulsTransaction.size();
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        if (null != lockNulsTransaction) {
            this.lockNulsTransaction.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        try {
            this.lockNulsTransaction = (LockNulsTransaction) TransactionManager.getInstance(byteBuffer);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
    }

    public LockNulsTransaction getLockNulsTransaction() {
        return lockNulsTransaction;
    }

    public void setLockNulsTransaction(LockNulsTransaction lockNulsTransaction) {
        this.lockNulsTransaction = lockNulsTransaction;
    }
}
