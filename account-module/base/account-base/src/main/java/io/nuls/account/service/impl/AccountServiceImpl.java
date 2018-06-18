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

package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.*;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.account.util.AccountTool;
import io.nuls.core.tools.crypto.*;
import io.nuls.core.tools.crypto.Exception.CryptoException;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.TransactionFeeCalculator;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Service
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AliasStorageService aliasStorageService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public Result<List<Account>> createAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "between 0 and 100 can be created at once");
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "Length between 8 and 20, the combination of characters and numbers");
        }
        locker.lock();
        try {
            List<Account> accounts = new ArrayList<>();
            List<AccountPo> accountPos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                if (StringUtils.isNotBlank(password)) {
                    account.encrypt(password);
                }
                accounts.add(account);
                AccountPo po = new AccountPo(account);
                accountPos.add(po);
            }
            if (accountStorageService == null) {
                Log.info("accountStorageService is null");
            }
            Result result = accountStorageService.saveAccountList(accountPos);
            if (result.isFailed()) {
                return result;
            }
            for(Account account : accounts) {
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
            return Result.getSuccess().setData(accounts);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "create account failed!");
        } finally {
            locker.unlock();
        }
    }

    @Override
    public Result<List<Account>> createAccount(String password) {
        return createAccount(1, password);
    }

    @Override
    public Result<List<Account>> createAccount(int count) {
        return createAccount(count, null);
    }

    @Override
    public Result<List<Account>> createAccount() {
        return createAccount(1, null);
    }

    @Override
    public Result<Boolean> removeAccount(String address, String password) {

        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (account == null) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //加过密(有密码)并且没有解锁, 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted() && account.isLocked()) {
            try {
                if (StringUtils.isBlank(password) || !StringUtils.validPassword(password) || !account.unlock(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        Result result = accountStorageService.removeAccount(account.getAddress());
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.remove(account.getAddress().getBase58());
        return Result.getSuccess();
    }

    @Override
    public Result<Account> updatePasswordByAccountKeyStore(AccountKeyStore keyStore, String password) {
        AssertUtil.canNotEmpty(keyStore, AccountErrorCode.PARAMETER_ERROR.getMsg());
        AssertUtil.canNotEmpty(keyStore.getAddress(), AccountErrorCode.PARAMETER_ERROR.getMsg());
        AssertUtil.canNotEmpty(password, AccountErrorCode.PARAMETER_ERROR.getMsg());
        Account account;
        byte[] priKey = null;
        if (null != keyStore.getPrikey() && keyStore.getPrikey().length > 0) {
            if (!ECKey.isValidPrivteHex(Hex.encode(keyStore.getPrikey()))) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            priKey = keyStore.getPrikey();
            try {
                account = AccountTool.createAccount(Hex.encode(priKey));
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.FAILED);
            }
        } else {
            try {
                account = AccountTool.createAccount();
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.FAILED);
            }
            account.setAddress(new Address(keyStore.getAddress()));
        }
        try {
            account.encrypt(password);
        } catch (NulsException e) {
            Log.error(e);
        }
        if (StringUtils.isNotBlank(keyStore.getAlias())) {
            Alias aliasDb = aliasService.getAlias(keyStore.getAlias());
            if (null != aliasDb && Base58.encode(aliasDb.getAddress()).equals(account.getAddress().toString())) {
                account.setAlias(aliasDb.getAlias());
            } else {
                List<AliasPo> list = aliasStorageService.getAliasList().getData();
                for (AliasPo aliasPo : list) {
                    //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                    if (Base58.encode(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                        account.setAlias(aliasPo.getAlias());
                        break;
                    }
                }
            }
        }

        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        accountLedgerService.importLedgerByAddress(account.getAddress().getBase58());
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password) {
        if (null == keyStore || null == keyStore.getAddress()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account;
        byte[] priKey = null;
        if (null != keyStore.getPrikey() && keyStore.getPrikey().length > 0) {
            if (!ECKey.isValidPrivteHex(Hex.encode(keyStore.getPrikey()))) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            priKey = keyStore.getPrikey();
        } else if (null == keyStore.getPrikey() && null != keyStore.getEncryptedPrivateKey()) {
            if (!StringUtils.validPassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            try {
                priKey = AESEncrypt.decrypt(Hex.decode(keyStore.getEncryptedPrivateKey()), password);
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        } else {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            account = AccountTool.createAccount(Hex.encode(priKey));
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        account.setAddress(new Address(keyStore.getAddress()));

        if (StringUtils.isNotBlank(keyStore.getAlias())) {
            Alias aliasDb = aliasService.getAlias(keyStore.getAlias());
            if (null != aliasDb && Base58.encode(aliasDb.getAddress()).equals(account.getAddress().toString())) {
                account.setAlias(aliasDb.getAlias());
            } else {
                List<AliasPo> list = aliasStorageService.getAliasList().getData();
                for (AliasPo aliasPo : list) {
                    //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                    if (Base58.encode(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                        account.setAlias(aliasPo.getAlias());
                        break;
                    }
                }
            }
        }
        account.setPubKey(keyStore.getPubKey());
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        accountLedgerService.importLedgerByAddress(account.getAddress().getBase58());
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore) {
        return importAccountFormKeyStore(keyStore, null);
    }

    @Override
    public Result<Account> importAccount(String prikey, String password) {
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account;
        try {
            account = AccountTool.createAccount(prikey);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        //扫所全网别名对比地址符合就设置
        //String alias = null;
        Account acc = getAccountByAddress(account.getAddress().toString());
        if (null == acc) {
            List<AliasPo> list = aliasStorageService.getAliasList().getData();
            for (AliasPo aliasPo : list) {
                //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                if (Base58.encode(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                    account.setAlias(aliasPo.getAlias());
                    break;
                }
            }
        } else {
            account.setAlias(acc.getAlias());
        }
        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        accountLedgerService.importLedgerByAddress(account.getAddress().getBase58());
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> importAccount(String prikey) {
        return importAccount(prikey, null);
    }


    @Override
    public Result<AccountKeyStore> exportAccountToKeyStore(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        //只要加过密(且没解锁),就验证密码
        if (account.isEncrypted() && account.isLocked()) {
            try {
                if (StringUtils.isBlank(password) || !StringUtils.validPassword(password) || !account.decrypt(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        //只要加过密(不管是否解锁)都不导出明文私钥, If the account is encrypted (regardless of unlocked), the plaintext private key is not exported
        if (account.isEncrypted()) {
            EncryptedData encryptedData = new EncryptedData(account.getEncryptedPriKey());
            accountKeyStore.setEncryptedPrivateKey(Hex.encode(encryptedData.getEncryptedBytes()));
        } else {
            accountKeyStore.setPrikey(account.getPriKey());
        }
        accountKeyStore.setAddress(account.getAddress().toString());
        accountKeyStore.setAlias(account.getAlias());
        accountKeyStore.setPubKey(account.getPubKey());
        return Result.getSuccess().setData(accountKeyStore);
    }


    /**
     * 根据账户地址字符串,获取账户对象(内部调用)
     * Get account object based on account address string
     *
     * @param address
     * @return Account
     */
    private Account getAccountByAddress(String address) {
        if (!Address.validAddress(address)) {
            return null;
        }
        //如果账户已经解锁,则直接返回解锁后的账户. If the account is unlocked, return directly to the unlocked account
        Account accountCache = accountCacheService.getAccountByAddress(address);
        if (null != accountCache) {
            return accountCache;
        }
        if(accountCacheService.localAccountMaps == null) {
            getAccountList();
        }
        return accountCacheService.localAccountMaps.get(address);
    }

    @Override
    public Result<Account> getAccount(byte[] address) {
        Account account = getAccountByAddress(Base58.encode(address));
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> getAccount(String address) {
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> getAccount(Address address) {
        if (null == address) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountByAddress(address.toString());
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Collection<Account>> getAccountList() {

        if(accountCacheService.localAccountMaps != null) {
            return Result.getSuccess().setData(accountCacheService.localAccountMaps.values());
        }
        accountCacheService.localAccountMaps = new ConcurrentHashMap<>();

        List<Account> list = new ArrayList<>();
        Result<List<AccountPo>> result = accountStorageService.getAccountList();
        if (result.isFailed()) {
            return Result.getFailed().setData(list);
        }
        List<AccountPo> poList = result.getData();
        Set<String> addressList = new HashSet<>();
        if (null == poList || poList.isEmpty()) {
            return Result.getSuccess().setData(list);
        }
        for (AccountPo po : poList) {
            Account account = po.toAccount();
            list.add(account);
            addressList.add(account.getAddress().getBase58());
        }
        for(Account account : list) {
            accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        }
        return Result.getSuccess().setData(list);
    }

    @Override
    public Result<Address> getAddress(String pubKey) {
        AssertUtil.canNotEmpty(pubKey, "");
        try {
            Address address = AccountTool.newAddress(ECKey.fromPublicOnly(Hex.decode(pubKey)));
            return Result.getSuccess().setData(address);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
    }

    @Override
    public Result<Address> getAddress(byte[] pubKey) {
        AssertUtil.canNotEmpty(pubKey, "");
        try {
            Address address = AccountTool.newAddress(pubKey);
            return Result.getSuccess().setData(address);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
    }

    @Override
    public Result<Boolean> isEncrypted(Account account) {
        if (null == account || null == account.getAddress()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (null == getAccountByAddress(account.getAddress().toString())) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return new Result(account.isEncrypted(), null);

    }

    @Override
    public Result<Boolean> isEncrypted(Address address) {
        return isEncrypted(address.toString());
    }

    @Override
    public Result<Boolean> isEncrypted(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        Result result = new Result();
        boolean rs = account.isEncrypted();
        return result.setSuccess(rs);
    }

    @Override
    public Result validPassword(Account account, String password) {
        if (null == account) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Result result = new Result();
        result.setSuccess(account.validatePassword(password));
        return result;
    }

    @Override
    public Result<Boolean> verifyAddressFormat(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return new Result(Address.validAddress(address), null);
    }

    @Override
    public NulsSignData signData(byte[] data, Account account, String password) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (account == null) {
            throw new NulsException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), account, password);
    }

    @Override
    public NulsSignData signData(byte[] data, Account account) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (account == null) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (null == account.getPriKey() || account.getPriKey().length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), account.getPriKey());
    }

    @Override
    public NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (null == ecKey) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), ecKey);
    }

    @Override
    public NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException {
        if (null == digest || digest.length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        //加过密(有密码)并且没有解锁, 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(password, "password can not be empty");
            try {
                return this.signDigest(digest, AESEncrypt.decrypt(account.getEncryptedPriKey(), password));
            } catch (CryptoException e) {
                throw new NulsException(AccountErrorCode.DECRYPT_ACCOUNT_ERROR);
            }
        } else {
            return this.signDigest(digest, account.getPriKey());
        }
    }

    private NulsSignData signDigest(byte[] digest, byte[] priKey) {
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(priKey));
        return signDigest(digest, ecKey);
    }

    @Override
    public NulsSignData signDigest(byte[] digest, ECKey ecKey) {
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);
        return nulsSignData;
    }

    @Override
    public Result verifySignData(byte[] data, NulsSignData signData, byte[] pubKey) {
        ECKey.verify(NulsDigestData.calcDigestData(data).getDigestBytes(), signData.getSignBytes(), pubKey);
        return new Result(true, null);
    }

    @Override
    public Result<Balance> getBalance(Account account) throws NulsException {
        if (null == account || null == account.getAddress()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return accountLedgerService.getBalance(account.getAddress().getBase58Bytes());
    }

    @Override
    public Result<Balance> getBalance(Address address) throws NulsException {
        if (null == address) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return accountLedgerService.getBalance(address.getBase58Bytes());
    }

    @Override
    public Result<Balance> getBalance(String address) throws NulsException {
        if (!Address.validAddress(address)) {
            Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        Address addr = new Address(address);
        return accountLedgerService.getBalance(addr.getBase58Bytes());
    }

    @Override
    public Result<Balance> getBalance() throws NulsException {
        List<Account> list = new ArrayList<>();
        Balance balance = new Balance();
        Result<List<AccountPo>> result = accountStorageService.getAccountList();
        if (result.isFailed()) {
            return Result.getFailed().setData(balance);
        }
        List<AccountPo> poList = result.getData();
        if (null == poList || poList.isEmpty()) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        for (AccountPo po : poList) {
            Account account = po.toAccount();
            list.add(account);
        }

        for (Account account : list) {
            Result<Balance> resultBalance = accountLedgerService.getBalance(account.getAddress().getBase58Bytes());
            if (resultBalance.isSuccess()) {
                Balance temp = resultBalance.getData();
                if (null == temp) {
                    continue;
                }
                balance.setBalance(balance.getBalance().add(temp.getBalance()));
                balance.setLocked(balance.getLocked().add(temp.getLocked()));
                balance.setUsable(balance.getUsable().add(temp.getUsable()));
            }
        }
        return Result.getSuccess().setData(balance);
    }

    @Override
    public Result<String> getAlias(byte[] address) {
        if(!Address.validAddress(address)){
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        return getAlias(Base58.encode(address));
    }

    @Override
    public Result<String> getAlias(String address) {
        Account account = getAccountByAddress(address);
        if(null != account){
            return Result.getSuccess().setData(account.getAlias());
        }
        String alias = null;
        List<AliasPo> list = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : list) {
            if (Base58.encode(aliasPo.getAddress()).equals(address)) {
                alias = aliasPo.getAlias();
                break;
            }
        }
        return Result.getSuccess().setData(alias);

    }

    @Override
    public Result<Na> getAliasFee(String addr, String aliasName) {
        if (!Address.validAddress(addr)) {
            Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = this.getAccount(addr).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
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
            Na fee = TransactionFeeCalculator.getMaxFee(tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
            return Result.getSuccess().setData(fee);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }
}
