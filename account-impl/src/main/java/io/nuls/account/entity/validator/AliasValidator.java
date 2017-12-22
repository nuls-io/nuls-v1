package io.nuls.account.entity.validator;

import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasValidator implements NulsDataValidator<AliasTransaction> {

    private static final AliasValidator INSTANCE = new AliasValidator();

    private AccountService accountService;

    private AliasValidator() {

    }

    public static AliasValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (StringUtils.isBlank(alias.getAddress()) || alias.getAddress().length() >= 30) {
            return ValidateResult.getFailedResult("The address format error");
        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult(" The alias format error");
        }

        Result result = accountService.canSetAlias(alias.getAddress(), alias.getAlias());
        if (!result.isSuccess()) {
            return ValidateResult.getFailedResult(result.getMessage());
        }
        return ValidateResult.getSuccessResult();
    }

    public void setAccountService(AccountService service) {
        this.accountService = service;
    }
}
