/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.entity.validator;

import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.context.NulsContext;
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

    private AliasDataService aliasDataService;

    private AliasValidator() {

    }

    public static AliasValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (StringUtils.isBlank(alias.getAddress()) || new Address(alias.getAddress()).getHash().length != 23) {
            return ValidateResult.getFailedResult("The address format error");
        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult("The alias is between 3 to 20 characters");
        }

        AliasPo aliasPo = getAliasDataService().get(alias.getAlias());
        if (aliasPo != null) {
            return ValidateResult.getFailedResult("The alias has been occupied");
        }
        return ValidateResult.getSuccessResult();
    }

    private AliasDataService getAliasDataService() {
        if (aliasDataService == null) {
            aliasDataService = NulsContext.getServiceBean(AliasDataService.class);
        }
        return aliasDataService;
    }
}
