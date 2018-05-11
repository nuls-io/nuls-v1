/*
package io.nuls.account.validator;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

*/
/**
 * @author: Charlie
 * @date: 2018/5/11
 *//*

public class AliasValidator implements NulsDataValidator<AliasTransaction> {

    private static final AliasValidator INSTANCE = new AliasValidator();

    private AccountStorageService aliasDataService;

    private LedgerService ledgerService;

    private AliasValidator() {

    }

    public static AliasValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(AliasTransaction tx) {
        Alias alias = tx.getTxData();
        if (!Address.validAddress(alias.getAddress())) {
            return ValidateResult.getFailedResult(ErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validAlias(alias.getAlias())) {
            return ValidateResult.getFailedResult(ErrorCode.ALIAS_ERROR);
        }

        long aliasValue = 0;
        UtxoData utxoData = (UtxoData) tx.getCoinData();
        for (UtxoInput input : utxoData.getInputs()) {
            aliasValue += input.getFrom().getValue();
        }

        if (aliasValue < AccountConstant.ALIAS_NA.getValue() + tx.getFee().getValue()) {
            return ValidateResult.getFailedResult(ErrorCode.INVALID_INPUT);
        }
//
//        if (tx.getStatus() == TxStatusEnum.UNCONFIRM) {
//            List<Transaction> txList = getLedgerService().getCacheTxList(TransactionConstant.TX_TYPE_SET_ALIAS);
//            if (txList != null && tx.size() > 0) {
//                for (Transaction trx : txList) {
//                    if (trx.getHash().equals(tx.getHash())) {
//                        continue;
//                    }
//                    Alias a = ((AliasTransaction) trx).getTxData();
//                    if (alias.getAddress().equals(a.getAddress())) {
//                        return ValidateResult.getFailedResult(ErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
//                    }
//                    if (alias.getAlias().equals(a.getAlias())) {
//                        return ValidateResult.getFailedResult("The alias has been occupied");
//                    }
//                }
//            }
//        }

        AliasPo aliasPo = getAliasDataService().get(alias.getAlias());
        if (aliasPo != null) {
            return ValidateResult.getFailedResult(ErrorCode.ALIAS_EXIST);
        }
        return ValidateResult.getSuccessResult();
    }

    private AliasDataService getAliasDataService() {
        if (aliasDataService == null) {
            aliasDataService = NulsContext.getServiceBean(AliasDataService.class);
        }
        return aliasDataService;
    }

    private LedgerService getLedgerService() {
        if (ledgerService == null) {
            ledgerService = NulsContext.getServiceBean(LedgerService.class);
        }
        return ledgerService;
    }
}
*/
