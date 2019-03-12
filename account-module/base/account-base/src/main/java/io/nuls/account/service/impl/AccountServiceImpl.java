/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.*;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.storage.service.MultiSigAccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.tools.crypto.AESEncrypt;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.EncryptedData;
import io.nuls.core.tools.crypto.Exception.CryptoException;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.script.ScriptBuilder;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Charlie
 */
@Service
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private MultiSigAccountStorageService multiSigAccountStorageService;

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
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);
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
            Result result = accountStorageService.saveAccountList(accountPos);
            if (result.isFailed()) {
                return result;
            }
            for (Account account : accounts) {
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
            return Result.getSuccess().setData(accounts);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(KernelErrorCode.FAILED);
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
    public Result removeAccount(String address, String password) {

        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (account == null) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //加过密(有密码)并且没有解锁, 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        Result result = accountStorageService.removeAccount(account.getAddress());
        if (result.isFailed()) {
            return result;
        }
        accountLedgerService.deleteUnconfirmedTx(account.getAddress().getAddressBytes());
        accountCacheService.localAccountMaps.remove(account.getAddress().getBase58());
        return Result.getSuccess().setData(true);
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
            return Result.getFailed(e.getErrorCode());
        }
        if (StringUtils.isNotBlank(keyStore.getAlias())) {
            Alias aliasDb = aliasService.getAlias(keyStore.getAlias());
            if (null != aliasDb && account.getAddress().toString().equals(AddressTool.getStringAddressByBytes(aliasDb.getAddress()))) {
                account.setAlias(aliasDb.getAlias());
            } else {
                List<AliasPo> list = aliasStorageService.getAliasList().getData();
                for (AliasPo aliasPo : list) {
                    //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                    if (AddressTool.getStringAddressByBytes(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                        account.setAlias(aliasPo.getAlias());
                        break;
                    }
                }
            }
        }
        account.setOk(false);
        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                String address = account.getAddress().getBase58();
                Result res = accountLedgerService.importLedger(address);
                if (res.isFailed()) {
                    AccountServiceImpl.this.removeAccount(address, password);
                } else {
                    AccountServiceImpl.this.finishImport(account);
                }
            }
        });
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password) {
        if (null == keyStore || StringUtils.isBlank(keyStore.getAddress())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!AddressTool.validAddress(keyStore.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
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
                return Result.getFailed(e.getErrorCode());
            }
            //如果私钥生成的地址和keystore的地址不相符，说明私钥错误
            if (!account.getAddress().getBase58().equals(keyStore.getAddress())) {
                return Result.getFailed(AccountErrorCode.PRIVATE_KEY_WRONG);
            }
        } else if (null == keyStore.getPrikey() && null != keyStore.getEncryptedPrivateKey()) {
            if (!StringUtils.validPassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            try {
                priKey = AESEncrypt.decrypt(Hex.decode(keyStore.getEncryptedPrivateKey()), password);
                account = AccountTool.createAccount(Hex.encode(priKey));
            } catch (CryptoException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            } catch (NulsException e) {
                return Result.getFailed(e.getErrorCode());
            }
            //如果私钥生成的地址和keystore的地址不相符，说明密码错误
            if (!account.getAddress().getBase58().equals(keyStore.getAddress())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        } else {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Alias aliasDb = null;
        if (StringUtils.isNotBlank(keyStore.getAlias())) {
            aliasDb = aliasService.getAlias(keyStore.getAlias());
        }
        if (null != aliasDb && AddressTool.getStringAddressByBytes(aliasDb.getAddress()).equals(account.getAddress().toString())) {
            account.setAlias(aliasDb.getAlias());
        } else {
            List<AliasPo> list = aliasStorageService.getAliasList().getData();
            for (AliasPo aliasPo : list) {
                //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                if (AddressTool.getStringAddressByBytes(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                    account.setAlias(aliasPo.getAlias());
                    break;
                }
            }
        }
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
                return Result.getFailed(e.getErrorCode());
            }
        }
        account.setOk(false);
        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                String address = account.getAddress().getBase58();
                Result res = accountLedgerService.importLedger(address);
                if (res.isFailed()) {
                    AccountServiceImpl.this.removeAccount(address, password);
                } else {
                    AccountServiceImpl.this.finishImport(account);
                }
            }
        });
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
            return Result.getFailed(AccountErrorCode.PRIVATE_KEY_WRONG);
        }
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
                return Result.getFailed(e.getErrorCode());
            }
        }
        //扫所全网别名对比地址符合就设置
        //String alias = null;
        Account acc = getAccountByAddress(account.getAddress().toString());
        if (null == acc) {
            List<AliasPo> list = aliasStorageService.getAliasList().getData();
            for (AliasPo aliasPo : list) {
                //如果全网别名中的地址有和当前导入的账户地址相同,则赋值别名到账户中
                if (AddressTool.getStringAddressByBytes(aliasPo.getAddress()).equals(account.getAddress().toString())) {
                    account.setAlias(aliasPo.getAlias());
                    break;
                }
            }
        } else {
            account.setAlias(acc.getAlias());
        }
//        Result res = accountLedgerService.importLedgerByAddress(account.getAddress().getBase58());
//        if (res.isFailed()) {
//            return res;
//        }
        account.setOk(false);
        AccountPo po = new AccountPo(account);
        Result result = accountStorageService.saveAccount(po);
        if (result.isFailed()) {
            return result;
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                String address = account.getAddress().getBase58();
                Result res = accountLedgerService.importLedger(address);
                if (res.isFailed()) {
                    AccountServiceImpl.this.removeAccount(address, password);
                } else {
                    AccountServiceImpl.this.finishImport(account);
                }
            }
        });
        return Result.getSuccess().setData(account);
    }

    private void finishImport(Account account) {
        account.setOk(true);
        accountStorageService.saveAccount(new AccountPo(account));
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
    }

    @Override
    public Result<Account> importAccount(String prikey) {
        return importAccount(prikey, null);
    }


    @Override
    public Result<AccountKeyStore> exportAccountToKeyStore(String address, String password) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        //只要加过密(且没解锁),就验证密码
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
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
     * @return Account
     */
    private Account getAccountByAddress(String address) {
        if (!AddressTool.validAddress(address)) {
            return null;
        }
        //如果账户已经解锁,则直接返回解锁后的账户. If the account is unlocked, return directly to the unlocked account
        Account accountCache = accountCacheService.getAccountByAddress(address);
        if (null != accountCache) {
            return accountCache;
        }
        if (accountCacheService.localAccountMaps == null) {
            getAccountList();
        }
        return accountCacheService.localAccountMaps.get(address);
    }

    @Override
    public Result<Account> getAccount(byte[] address) {
        if (null == address || address.length == 0) {
            return Result.getFailed(AccountErrorCode.NULL_PARAMETER);
        }
        String addr = AddressTool.getStringAddressByBytes(address);
        if (!AddressTool.validAddress(addr)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        return getAccount(addr);
    }

    @Override
    public Result<Account> getAccount(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result<Account> getAccount(Address address) {
        if (null == address) {
            return Result.getFailed(AccountErrorCode.NULL_PARAMETER);
        }
        return getAccount(address.toString());
    }

    @Override
    public Result<Collection<Account>> getAccountList() {
        List<Account> list = new ArrayList<>();
        if (accountCacheService.localAccountMaps != null) {
            Collection<Account> values = accountCacheService.localAccountMaps.values();
            Iterator<Account> iterator = values.iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
        } else {
            accountCacheService.localAccountMaps = new ConcurrentHashMap<>();
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
            for (Account account : list) {
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        }
        list.sort(new Comparator<Account>() {
            @Override
            public int compare(Account o1, Account o2) {
                return (o2.getCreateTime().compareTo(o1.getCreateTime()));
            }
        });
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
    public Result isEncrypted(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        Result result = new Result();
        boolean rs = account.isEncrypted();
        result.setSuccess(true);
        result.setData(rs);
        return result;
    }

    @Override
    public NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException {
        if (null == digest || digest.length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
        }
        //加过密(有密码)并且没有解锁, 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(password, "password can not be empty");
            return this.signDigest(digest, account.getPriKey(password));
        } else {
            return this.signDigest(digest, account.getPriKey());
        }
    }

    private NulsSignData signDigest(byte[] digest, byte[] priKey) {
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1, priKey));
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
            Result<Balance> resultBalance = accountLedgerService.getBalance(account.getAddress().getAddressBytes());
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
        return getAlias(AddressTool.getStringAddressByBytes(address));
    }

    @Override
    public Result<String> getAlias(String address) {
        Account account = getAccountByAddress(address);
        if (null != account) {
            return Result.getSuccess().setData(account.getAlias());
        }
        String alias = null;
        List<AliasPo> list = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : list) {
            if (AddressTool.getStringAddressByBytes(aliasPo.getAddress()).equals(address)) {
                alias = aliasPo.getAlias();
                break;
            }
        }
        return Result.getSuccess().setData(alias);
    }

    @Override
    public Result<Address> createMultiAccount(List<String> pubkeys, int m) {
        locker.lock();
        try {
            Script redeemScript = ScriptBuilder.createNulsRedeemScript(m, pubkeys);
            Address address = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
            MultiSigAccount account = new MultiSigAccount();
            account.setAddress(address);
            account.setM(m);
            account.addPubkeys(pubkeys);
            Result result = this.multiSigAccountStorageService.saveAccount(account.getAddress(), account.serialize());
            if (result.isFailed()) {
                return result;
            }
            return result.setData(account);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(KernelErrorCode.FAILED);
        } finally {
            locker.unlock();
        }
    }

    /**
     * 获取所有账户集合
     * Query all account collections.
     *
     * @return account list of all accounts.
     */
    @Override
    public Result<List<MultiSigAccount>> getMultiSigAccountList() {
        List<byte[]> list = this.multiSigAccountStorageService.getAccountList().getData();
        if (null == list) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        List<MultiSigAccount> accountList = new ArrayList<>();
        for (byte[] bytes : list) {
            MultiSigAccount account = new MultiSigAccount();
            try {
                account.parse(new NulsByteBuffer(bytes, 0));
            } catch (NulsException e) {
                Log.error(e);
            }
            accountList.add(account);
        }
        List<AliasPo> aliasList = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : aliasList) {
            if (aliasPo.getAddress()[2] != NulsContext.P2SH_ADDRESS_TYPE) {
                continue;
            }
            for (MultiSigAccount multiSigAccount : accountList) {
                if (Arrays.equals(aliasPo.getAddress(), multiSigAccount.getAddress().getAddressBytes())) {
                    multiSigAccount.setAlias(aliasPo.getAlias());
                    break;
                }
            }
        }
        return new Result<List<MultiSigAccount>>().setData(accountList);
    }

    /**
     * 根据地址获取本地存储的多签账户的详细信息
     * Get the details of the locally stored multi-sign account based on the address
     *
     * @param address 多签地址
     * @return 多签账户的详细信息
     */
    @Override
    public Result<MultiSigAccount> getMultiSigAccount(String address) throws Exception {
        byte[] bytes = this.multiSigAccountStorageService.getAccount(Address.fromHashs(address)).getData();
        if (null == bytes) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        MultiSigAccount account = new MultiSigAccount();
        account.parse(new NulsByteBuffer(bytes, 0));
        List<AliasPo> list = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : list) {
            if (aliasPo.getAddress()[2] != NulsContext.P2SH_ADDRESS_TYPE) {
                continue;
            }
            if (Arrays.equals(aliasPo.getAddress(), account.getAddress().getAddressBytes())) {
                account.setAlias(aliasPo.getAlias());
                break;
            }
        }
        return Result.getSuccess().setData(account);
    }

    /**
     * 导入一个跟本地地址相关的多签账户
     *
     * @param addressStr 多签地址
     * @param pubkeys    多签组成公钥列表
     * @param m          最小签名数
     * @return 是否成功
     */
    @Override
    public Result<Boolean> saveMultiSigAccount(String addressStr, List<String> pubkeys, int m) {
        Script redeemScript = ScriptBuilder.createNulsRedeemScript(m, pubkeys);
        Address address = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
        if (!AddressTool.getStringAddressByBytes(address.getAddressBytes()).equals(addressStr)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        MultiSigAccount account = new MultiSigAccount();
        account.setAddress(address);
        account.setM(m);
        account.addPubkeys(pubkeys);
        Result result = null;
        try {
            result = this.multiSigAccountStorageService.saveAccount(account.getAddress(), account.serialize());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SERIALIZE_ERROR);
        }
        if (result.isFailed()) {
            return result;
        }
        return result.setData(addressStr);
    }

    /**
     * 从数据库中删除该账户
     */
    @Override
    public Result<Boolean> removeMultiSigAccount(String address) {
        try {
            Address addressObj = Address.fromHashs(address);
            Result result = this.multiSigAccountStorageService.getAccount(addressObj);
            if (result.isFailed() || result.getData() == null) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            return this.multiSigAccountStorageService.removeAccount(addressObj);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }
}
