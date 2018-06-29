/*
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
 *
 */

package io.nuls.account.validator;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.kernel.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Charlie
 */
@Component
public class AliasTransactionValidator implements NulsDataValidator<AliasTransaction> {

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AliasStorageService aliasStorageService;

    @Autowired
    private AccountLedgerService accountledgerService;

    @Autowired
    private AccountService accountService;

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (!aliasService.isAliasUsable(alias.getAlias())) {
            return ValidateResult.getFailedResult(this.getClass().getName(), AccountErrorCode.ALIAS_EXIST);
        }
        List<AliasPo> list = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : list) {
            if (Arrays.equals(aliasPo.getAddress(), alias.getAddress())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
            }
        }
//        if (!AddressTool.validAddress(alias.getAddress())) {
//            return ValidateResult.getFailedResult(this.getClass().getName(), AccountErrorCode.ADDRESS_ERROR);
//        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult(this.getClass().getName(), AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        AliasPo aliasPo = aliasStorageService.getAlias(alias.getAlias()).getData();
        if (aliasPo != null) {
            return ValidateResult.getFailedResult(this.getClass().getName(), AccountErrorCode.ALIAS_EXIST);
        }
        if (tx.isSystemTx()) {
            return ValidateResult.getFailedResult(alias.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        CoinData coinData = tx.getCoinData();
        if (null == coinData) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        P2PKHScriptSig sig = new P2PKHScriptSig();
        try {
            sig.parse(tx.getScriptSig(), 0);
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getErrorCode());
        }
        if (!Arrays.equals(tx.getTxData().getAddress(), AddressTool.getAddress(sig.getPublicKey()))) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.DATA_ERROR);
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }

}
