package io.nuls.account.entity.validator;

import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.entity.AliasPo;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasValidator implements NulsDataValidator<AliasTransaction> {

    private static final AliasValidator INSTANCE = new AliasValidator();

    private AliasDataService aliasDao;

    private AliasValidator() {

    }

    public static AliasValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (StringUtils.isBlank(alias.getAddress()) || new Address(alias.getAddress()).getHash160().length > 25) {
            return ValidateResult.getFailedResult("The address format error");
        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult("The alias is between 3 to 20 characters");
        }

        AliasPo aliasPo = aliasDao.get(alias.getAlias());
        if (aliasPo != null) {
            return ValidateResult.getFailedResult("The alias has been occupied");
        }
        return ValidateResult.getSuccessResult();
    }

    public void setAliasDao(AliasDataService aliasDao) {
        this.aliasDao = aliasDao;
    }
}
