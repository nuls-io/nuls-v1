package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.validator.consensus.AccountCreditValidator;
import io.nuls.consensus.entity.validator.consensus.AgentDepositValidator;
import io.nuls.consensus.tx.AbstractConsensusTransaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends LockNulsTransaction<Consensus<Agent>> {

    public RegisterAgentTransaction() {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT);
        this.registerValidator(new AccountCreditValidator());
        this.registerValidator(new AgentDepositValidator());
    }

    public RegisterAgentTransaction(CoinTransferData lockData, String password) {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT, lockData, password);
    }

    @Override
    protected Consensus<Agent> parseBody(NulsByteBuffer byteBuffer) {
        Consensus<Agent> consensus = new Consensus<>();
        consensus.parse(byteBuffer);
        Agent delegate = new Agent();
        delegate.parse(byteBuffer);
        consensus.setExtend(delegate);
        return consensus;
    }

}
