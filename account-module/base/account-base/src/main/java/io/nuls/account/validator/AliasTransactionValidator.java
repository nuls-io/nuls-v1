package io.nuls.account.validator;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;

/**
 * @author: Charlie
 * @date: 2018/5/11
 */
@Component
public class AliasTransactionValidator implements NulsDataValidator<AliasTransaction> {

    @Autowired
    private AliasService accountBaseService;

    @Autowired
    private AliasStorageService alisaStorageService;

    @Autowired
    private AccountLedgerService accountledgerService;

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (!Address.validAddress(alias.getAddress())) {
            return ValidateResult.getFailedResult(alias.getClass().getName(), AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult(alias.getClass().getName(), AccountErrorCode.ALIAS_ERROR);
        }
        AliasPo aliasPo = alisaStorageService.getAlias(alias.getAlias()).getData();
        if (aliasPo != null) {
            return ValidateResult.getFailedResult(AliasPo.class.getName(), AccountErrorCode.ALIAS_EXIST);
        }

       /*if (!tx.isFreeOfFee()) {
            return ValidateResult.getFailedResult(alias.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }*/
        CoinData coinData = tx.getCoinData();
        if (null == coinData) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        Na realFee = tx.getFee();
        Na fee = TransactionFeeCalculator.getFee(tx.size());
        if (realFee.isLessThan(fee.add(AccountConstant.ALIAS_NA))) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        P2PKHScriptSig sig = new P2PKHScriptSig();
        try {
            sig.parse(tx.getScriptSig());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (!Arrays.equals(tx.getTxData().getAddress(), AddressTool.getAddress(sig.getPublicKey()))) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The agent does not belong to this address.");
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }

}
