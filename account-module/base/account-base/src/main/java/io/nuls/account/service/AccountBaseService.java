package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.accountLedger.service.AccountLedgerService;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.validate.ValidateResult;

import java.util.ArrayList;
import java.util.List;

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
     * @param addr Address of account
     * @param password password of account
     * @param aliasName the alias to set
     * @return
     */
    public Result<Boolean> setAlias(String addr, String password, String aliasName){
        if(!Address.validAddress(addr)){
            Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
        Address address = new Address(addr);
        Account account = accountService.getAccount(address).getData();
        if(null == account){
            Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return new Result(false, "Alias has been set up");
        }
        if (!StringUtils.validAlias(aliasName)) {
            return new Result(false, "The alias is between 3 to 20 characters");
        }
        byte[] addressBytes = address.getBase58Bytes();
        try {

            //手续费 fee ////////暂时!!!!
            Na fee = Na.parseNuls(0.01);
            Na total = fee.add(AccountConstant.ALIAS_NA);
            List<Coin> fromList = accountLedgerService.getCoinData(addressBytes, total);
            Na totalFrom = Na.ZERO;
            for (Coin coin : fromList){
                totalFrom = totalFrom.add(coin.getNa());
            }
            if(totalFrom.isLessThan(total)){
                Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(fromList);
            if(totalFrom.isGreaterThan(total)){
                //创建toList
                List<Coin> toList = new ArrayList<>();
                Na change = totalFrom.minus(total);
                toList.add(new Coin(address.getBase58Bytes(), change,0));
                coinData.setTo(toList);
            }

            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(System.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));

            NulsSignData nulsSignData = accountService.signData(tx.serializeForHash(), account, password);
            P2PKHScriptSig scriptSig = new P2PKHScriptSig(nulsSignData, account.getPubKey());
            tx.setScriptSig(scriptSig.serialize());

//            ValidateResult validate = this.ledgerService.verifyTx(aliasTx,this.ledgerService.getWaitingTxList());
//            if (validate.isFailed()) {
//                return new Result(false, validate.getMessage());
//            }
//            TransactionMessage message = new TransactionMessage();
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
        return null;
    }


    /*public Result<Alias> getAlias(Account account){
        return this.getAlias(account.getAddress());
    }

    public Result<Alias> getAlias(Address address){
        return getAlias(address.getBase58Bytes());
    }
    public Result<Alias> getAlias(String address){
        return getAlias(new Address(address));
    }*/
  /*  public Result<Alias> getAlias(byte[] address){
        Account account = accountService.getAccount(address).getData();
        if(null == account){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return Result.getSuccess().setData()
    }*/

}
