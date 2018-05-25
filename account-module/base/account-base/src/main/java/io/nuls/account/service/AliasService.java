package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.ledger.service.LedgerService;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.service.TransactionService;

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
    private AccountService accountService;

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AliasStorageService aliasStorageService;

    @Autowired
    private MessageBusService messageBusService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    /**
     * 设置别名
     * Initiate a transaction to set alias.
     *
     * @param addr      Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return txhash
     */
    public Result<String> setAlias(String addr, String aliasName, String password) {
        if (!Address.validAddress(addr)) {
            Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(addr).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            if (account.isEncrypted() && account.isLocked()) {
                if (StringUtils.isBlank(password) || !StringUtils.validPassword(password) || !account.unlock(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS, "Alias has been set up");
        }
        if (!StringUtils.validAlias(aliasName)) {
            return Result.getFailed("The alias is between 3 to 20 characters");
        }
        if (isAliasExist(aliasName)) {
            return Result.getFailed(AccountErrorCode.ALIAS_EXIST);
        }
        byte[] addressBytes = account.getAddress().getBase58Bytes();
        try {
            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(System.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
            NulsSignData nulsSignData = accountService.signData(tx.serializeForHash(), account, password);
            P2PKHScriptSig scriptSig = new P2PKHScriptSig(nulsSignData, account.getPubKey());
            tx.setScriptSig(scriptSig.serialize());
            Result saveResult = accountLedgerService.saveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                return saveResult;
            }
            Result sendResult = this.transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                return sendResult;
            }
            String hash = tx.getHash().getDigestHex();
            return Result.getSuccess().setData(hash);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    /**
     * 保存别名(全网)
     * 1.保存别名alias至数据库
     * 2.从数据库取出对应的account账户,将别名设置进account然后保存至数据库
     * 3.将修改后的account重新进行缓存
     * saveAlias
     * 1. Save the alias to the database.
     * 2. Take the corresponding account from the database, set the alias to account and save it to the database.
     * 3. Re-cache the modified account.
     *
     * @param aliaspo
     * @return
     */
    public Result saveAlias(AliasPo aliaspo) throws NulsException {
        try {
            Result result = aliasStorageService.saveAlias(aliaspo);
            if (result.isFailed()) {
                this.rollbackAlias(aliaspo);
            }
            AccountPo po = accountStorageService.getAccount(aliaspo.getAddress()).getData();
            if (null != po) {
                po.setAlias(aliaspo.getAlias());
                Result resultAcc = accountStorageService.updateAccount(po);
                if (resultAcc.isFailed()) {
                    this.rollbackAlias(aliaspo);
                }
            }
        } catch (Exception e) {
            this.rollbackAlias(aliaspo);
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        return Result.getSuccess();
    }

    public Alias getAlias(String alias) {
        AliasPo aliasPo = aliasStorageService.getAlias(alias).getData();
        return aliasPo == null ? null : aliasPo.toAlias();
    }

    public boolean isAliasExist(String alias) {
        return null != getAlias(alias);
    }

    /**
     * 回滚别名操作(删除别名(全网))
     * 1.从数据库删除别名对象数据
     * 2.取出对应的account将别名清除,重新存入数据库
     * 3.重新缓存account
     * rollbackAlias
     * 1.Delete the alias data from the database.
     * 2. Remove the corresponding account to clear the alias and restore it in the database.
     * 3. Recache the account.
     *
     * @param aliasPo
     * @return
     */
    public Result rollbackAlias(AliasPo aliasPo) throws NulsException {
        try {
            AliasPo po = aliasStorageService.getAlias(aliasPo.getAlias()).getData();
            if (po != null && Base58.encode(po.getAddress()).equals(Base58.encode(aliasPo.getAddress()))) {
                aliasStorageService.removeAlias(aliasPo.getAlias());
                Result<AccountPo> rs = accountStorageService.getAccount(aliasPo.getAddress());
                if(rs.isSuccess()) {
                    AccountPo accountPo = rs.getData();
                    accountPo.setAlias("");
                    Result result = accountStorageService.updateAccount(accountPo);
                    if(result.isFailed()){
                        return Result.getFailed(AccountErrorCode.FAILED);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
        }
        return Result.getSuccess();
    }


}
