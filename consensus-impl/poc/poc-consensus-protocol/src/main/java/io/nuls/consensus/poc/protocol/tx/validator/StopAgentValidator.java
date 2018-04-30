package io.nuls.consensus.poc.protocol.tx.validator;

import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;

/**
 * @author: Niels Wang
 * @date: 2018/4/30
 */
public class StopAgentValidator implements NulsDataValidator<StopAgentTransaction> {

    private static final StopAgentValidator INSTANCE = new StopAgentValidator();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

    private StopAgentValidator() {
    }

    public static StopAgentValidator getInstance() {
        return INSTANCE;
    }

    /**
     * @param data
     * @return
     */
    @Override
    public ValidateResult validate(StopAgentTransaction data) {
        if (data == null || data.getTxData() == null) {
            return ValidateResult.getFailedResult(ErrorCode.DATA_ERROR, "stop agent tx is null!");
        }
        NulsDigestData joinTxHash = data.getTxData();
        Transaction tx = ledgerService.getTx(joinTxHash);
        RegisterAgentTransaction agentTransaction = (RegisterAgentTransaction) tx;
        if (tx == null || agentTransaction.getTxData() == null || agentTransaction.getTxData().getHash() == null || tx.getBlockHeight() < 0) {
            return ValidateResult.getFailedResult(ErrorCode.FAILED, "The agent is not exist!");
        }
        AgentPo po = agentDataService.get(agentTransaction.getTxData().getHexHash());
        if (null == po || po.getDelHeight() > 0) {
            return ValidateResult.getFailedResult(ErrorCode.FAILED, "the agent was deleted!");
        }

        return ValidateResult.getSuccessResult();
    }
}
