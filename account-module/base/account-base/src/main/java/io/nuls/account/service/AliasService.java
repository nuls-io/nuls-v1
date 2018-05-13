package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.model.Balance;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.accountLedger.service.AccountLedgerService;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.message.TransactionMessage;

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
public class AliasService {

    @Autowired
    public AccountService accountService;

    @Autowired
    public AccountStorageService accountStorageService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AliasStorageService aliasStorageService;

    @Autowired
    private AccountCacheService AccountCacheService;

    @Autowired
    private MessageBusService messageBusService;
    /**
     * 设置别名
     * Set an alias for the account.
     *
     * @param addr      Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return
     */
    public Result<Boolean> setAlias(String addr, String password, String aliasName) {
        if (!Address.validAddress(addr)) {
            Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
        Address address = new Address(addr);
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return new Result(false, "Alias has been set up");
        }
        if (!StringUtils.validAlias(aliasName)) {
            return new Result(false, "The alias is between 3 to 20 characters");
        }
        if (isAliasExist(aliasName)) {
            Result.getFailed(AccountErrorCode.ALIAS_EXIST);
        }
        byte[] addressBytes = address.getBase58Bytes();
        try {
            //手续费 fee ////////暂时!!!!
            Na fee = Na.parseNuls(0.01);
            Na total = fee.add(AccountConstant.ALIAS_NA);
            Balance balance = accountLedgerService.getBalance(address.getBase58Bytes()).getData();
            if (balance.getUsable().isLessThan(total)) {
                Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            List<Coin> fromList = accountLedgerService.getCoinData(addressBytes, total);
            Na totalFrom = Na.ZERO;
            for (Coin coin : fromList) {
                totalFrom = totalFrom.add(coin.getNa());
            }
            if (totalFrom.isLessThan(total)) {
                Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(fromList);
            if (totalFrom.isGreaterThan(total)) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                Na change = totalFrom.minus(total);
                toList.add(new Coin(address.getBase58Bytes(), change, 0));
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
/*            ValidateResult validate = AliasTransactionValidator.getInstance().validate(tx);
            ValidateResult validate = this.ledgerService.verifyTx(aliasTx,this.ledgerService.getWaitingTxList());
            if (validate.isFailed()) {
                return new Result(false, validate.getMessage());
            }*/
            TransactionMessage message = new TransactionMessage();
            message.setMsgBody(tx);
//            this.ledgerService.saveLocalTx(aliasTx);
//            boolean b  = messageBusService.publishToLocal(event);
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

    public Alias getAlias(String alias) {
        AliasPo aliasPo = aliasStorageService.getAlias(alias).getData();
        return aliasPo == null ? null : aliasPo.toAlias();
    }

    public boolean isAliasExist(String alias) {
        return null != getAlias(alias);
    }

    public void rollbackAlias(AliasPo aliasPo) {
        AliasPo po = aliasStorageService.getAlias(aliasPo.getAlias()).getData();
        if (po != null && po.getAddress().equals(aliasPo.getAddress())) {
            aliasStorageService.removeAlias(aliasPo.getAlias());
            AccountPo accountPo = accountStorageService.getAccount(aliasPo.getAddress()).getData();
            accountPo.setAlias("");
            accountStorageService.updateAccount(accountPo);
            AccountCacheService.putAccount(accountPo.toAccount());
        }
    }


}
