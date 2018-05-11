package io.nuls.account.service;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.accountLedger.service.AccountLedgerService;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.message.TransactionMessage;

/**
 * 账户模块内部功能服务类
 * Account module internal function service class
 *
 * @author: Charlie
 * @date: 2018/5/11
 */
@Service
public class AccountBaseService {

    @Autowired
    public AccountService accountService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    /**
     * 设置别名
     * Set an alias for the account.
     * @param address Address of account
     * @param password password of account
     * @param alias the alias to set
     * @return
     */
   /* public Result setAlias(String address, String password, String alias){
        if(!Address.validAddress(address)){
            Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if(null == account){
            Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return new Result(false, "Alias has been set up");
        }
        if (!StringUtils.validAlias(alias)) {
            return new Result(false, "The alias is between 3 to 20 characters");
        }

        try {
            //TransactionMessage event = new TransactionMessage();
            TransactionMessage message = new TransactionMessage();
            CoinTransferData coinData = new CoinTransferData(OperationType.TRANSFER, AccountConstant.ALIAS_NA, address, ledgerService.getTxFee(TransactionConstant.TX_TYPE_SET_ALIAS));
            AliasTransaction aliasTx = new AliasTransaction(coinData, password, new Alias(address, alias));
//            aliasTx.setHash(NulsDigestData.calcDigestData(aliasTx.serialize()));
//            aliasTx.setScriptSig(createP2PKHScriptSigFromDigest(aliasTx.getHash(), account, password).serialize());
//            ValidateResult validate = this.ledgerService.verifyTx(aliasTx,this.ledgerService.getWaitingTxList());
//            if (validate.isFailed()) {
//                return new Result(false, validate.getMessage());
//            }
//
//            //event.setEventBody(aliasTx);
//            message.setMsgBody(aliasTx);
//            this.ledgerService.saveLocalTx(aliasTx);
//            boolean b  = eventBroadcaster.publishToLocal(event);
//            if(b){
//                return Result.getSuccess();
//            }else{
//                return Result.getFailed("publish failed!");
//            }
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, e.getMessage());
        }
    }*/
}
