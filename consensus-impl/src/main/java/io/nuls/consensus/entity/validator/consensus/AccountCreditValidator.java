package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AccountCreditValidator implements NulsDataValidator<RegisterAgentTransaction> {

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    @Override
    public ValidateResult validate(RegisterAgentTransaction data) {
        List<Transaction> list = null;
        try {
            //todo            list = ledgerService.getListByAddress(data.getTxData().getAddress(), TransactionConstant.TX_TYPE_RED_PUNISH,0,0);
        } catch (Exception e) {
            Log.error(e);
        }
        if(null!=list&&!list.isEmpty()){
            return ValidateResult.getFailedResult(ErrorCode.LACK_OF_CREDIT);
        }
        return ValidateResult.getSuccessResult();
    }
}
