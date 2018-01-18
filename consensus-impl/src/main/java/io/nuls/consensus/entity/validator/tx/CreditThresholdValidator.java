package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class CreditThresholdValidator implements NulsDataValidator<PocJoinConsensusTransaction> {

    private static final CreditThresholdValidator INSTANCE = new CreditThresholdValidator();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private CreditThresholdValidator() {
    }

    public static CreditThresholdValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(PocJoinConsensusTransaction data) {
        String address = data.getTxData().getAddress();
        List<Transaction> list = null;
        try {
            list = ledgerService.getListByAddress(address, TransactionConstant.TX_TYPE_RED_PUNISH, 0, 0);
        } catch (Exception e) {
            Log.error(e);
        }
        if (null == list || list.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        return ValidateResult.getFailedResult(ErrorCode.FAILED);
    }
}
