package io.nuls.consensus.entity.tx;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.validator.consensus.AccountCreditValidator;
import io.nuls.consensus.entity.validator.consensus.AgentDepositValidator;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends AbstractCoinTransaction<Consensus<Agent>> {

    public RegisterAgentTransaction() {
        this(null, null);
    }

    public RegisterAgentTransaction(CoinTransferData lockData, String password) {
        super(TransactionConstant.TX_TYPE_REGISTER_AGENT, lockData, password);
        this.registerValidator(new AccountCreditValidator());
        this.registerValidator(new AgentDepositValidator());
    }

    @Override
    protected Consensus<Agent> parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        Consensus<Agent> consensus = byteBuffer.readNulsData(new Consensus<>());
        Agent delegate = byteBuffer.readNulsData(new Agent());
        consensus.setExtend(delegate);
        return consensus;
    }

}
