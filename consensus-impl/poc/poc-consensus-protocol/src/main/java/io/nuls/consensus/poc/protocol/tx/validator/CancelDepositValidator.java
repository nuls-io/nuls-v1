package io.nuls.consensus.poc.protocol.tx.validator;

import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;

/**
 * @author: Niels Wang
 * @date: 2018/4/30
 */
public class CancelDepositValidator implements NulsDataValidator<CancelDepositTransaction> {

    private static final CancelDepositValidator INSTANCE = new CancelDepositValidator();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);

    private CancelDepositValidator() {
    }

    public static CancelDepositValidator getInstance() {
        return INSTANCE;
    }

    /**
     * @param data
     * @return
     */
    @Override
    public ValidateResult validate(CancelDepositTransaction data) {
        if (data == null || data.getTxData() == null) {
            return ValidateResult.getFailedResult(ErrorCode.DATA_ERROR, "deposit tx is null!");
        }
        NulsDigestData joinTxHash = data.getTxData();
        Transaction tx = ledgerService.getTx(joinTxHash);
        PocJoinConsensusTransaction joinTx = (PocJoinConsensusTransaction) tx;
        if (tx == null || joinTx.getTxData() == null || joinTx.getTxData().getHash() == null || tx.getBlockHeight() < 0) {
            return ValidateResult.getFailedResult(ErrorCode.FAILED, "The deposit is not exist!");
        }
        DepositPo po = depositDataService.get(joinTx.getTxData().getHexHash());
        if (null == po || po.getDelHeight() > 0) {
            return ValidateResult.getFailedResult(ErrorCode.FAILED, "the deposit was deleted!");
        }

        return ValidateResult.getSuccessResult();
    }
}
