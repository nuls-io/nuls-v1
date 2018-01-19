/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.constant.PunishReasonEnum;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.CoinDataProvider;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class RedPunishValidator implements NulsDataValidator<RedPunishTransaction> {

    private static final RedPunishValidator INSTANCE = new RedPunishValidator();

    private CoinDataProvider coinDataProvider = NulsContext.getInstance().getService(CoinDataProvider.class);

    private RedPunishValidator() {
    }

    public static RedPunishValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(RedPunishTransaction data) {
        if (data.getTxData().getReasonCode() != PunishReasonEnum.DOUBLE_SPEND.getCode()) {
            return ValidateResult.getSuccessResult();
        }
        return  data.getTxData().verify();
    }
}
