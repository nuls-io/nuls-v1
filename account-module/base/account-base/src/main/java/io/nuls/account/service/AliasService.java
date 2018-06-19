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

package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.TransactionFeeCalculator;
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
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
        }
        if (!StringUtils.validAlias(aliasName)) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (isAliasExist(aliasName)) {
            return Result.getFailed(AccountErrorCode.ALIAS_EXIST);
        }
        byte[] addressBytes = account.getAddress().getBase58Bytes();
        try {
            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
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

            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
            coinData.addTo(coin);

            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            NulsSignData nulsSignData = accountService.signDigest(tx.getHash().getDigestBytes(), account, password);
            P2PKHScriptSig scriptSig = new P2PKHScriptSig(nulsSignData, account.getPubKey());
            tx.setScriptSig(scriptSig.serialize());
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                return saveResult;
            }
            Result sendResult = this.transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                accountLedgerService.rollbackTransaction(tx);
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
                Account account = po.toAccount();
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
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
     */
    public Result rollbackAlias(AliasPo aliasPo) throws NulsException {
        try {
            AliasPo po = aliasStorageService.getAlias(aliasPo.getAlias()).getData();
            if (po != null && Base58.encode(po.getAddress()).equals(Base58.encode(aliasPo.getAddress()))) {
                aliasStorageService.removeAlias(aliasPo.getAlias());
                Result<AccountPo> rs = accountStorageService.getAccount(aliasPo.getAddress());
                if (rs.isSuccess()) {
                    AccountPo accountPo = rs.getData();
                    accountPo.setAlias("");
                    Result result = accountStorageService.updateAccount(accountPo);
                    if (result.isFailed()) {
                        return Result.getFailed(AccountErrorCode.FAILED);
                    }
                    Account account = accountPo.toAccount();
                    accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
        }
        return Result.getSuccess();
    }


}
