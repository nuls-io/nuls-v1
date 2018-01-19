package io.nuls.ledger.validator;

import io.nuls.account.service.intf.AccountService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class UtxoTxInputsValidator implements NulsDataValidator<UtxoData> {
    private static final UtxoTxInputsValidator INSTANCE = new UtxoTxInputsValidator();

    private UtxoTxInputsValidator() {

    }

    public static UtxoTxInputsValidator getInstance() {
        return INSTANCE;
    }

    private AccountService accountService;


    @Override
    public ValidateResult validate(UtxoData data) {

        for (int i = 0; i < data.getInputs().size(); i++) {

        }
        return null;
    }


    private AccountService getAccountService() {
        if (accountService == null) {
            accountService = NulsContext.getInstance().getService(AccountService.class);
        }
        return accountService;
    }
}