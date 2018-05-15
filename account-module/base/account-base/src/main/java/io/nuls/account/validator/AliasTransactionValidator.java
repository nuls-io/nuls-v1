package io.nuls.account.validator;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.accountLedger.service.AccountLedgerService;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

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

        if (!tx.isFreeOfFee()) {
            return ValidateResult.getFailedResult(alias.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        CoinData coinData = tx.getCoinData();
        if (null == coinData) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        Na realFee = TransactionFeeCalculator.calcFee(tx);
        Na fee = TransactionFeeCalculator.getFee(tx.size());
        if (realFee.isLessThan(fee.add(AccountConstant.ALIAS_NA))) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.FEE_NOT_RIGHT);
        }
        return ValidateResult.getSuccessResult();

    }

}
