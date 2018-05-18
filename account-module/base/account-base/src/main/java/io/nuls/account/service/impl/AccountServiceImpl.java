package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.AccountKeyStore;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.core.tools.crypto.*;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Component
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    /**
     * 本地账户集合
     * Collection of local accounts
     */
    public static Set<String> LOCAL_ADDRESS_LIST = ConcurrentHashMap.newKeySet();

    @Override
    public Result<List<Account>> createAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "between 0 and 100 can be created at once");
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "Length between 8 and 20, the combination of characters and numbers");
        }
        locker.lock();
        try {
            List<Account> accounts = new ArrayList<>();
            List<AccountPo> accountPos = new ArrayList<>();
            List<String> resultList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                if (null != password) {
                    account.encrypt(password);
                }
                accounts.add(account);
                AccountPo po = new AccountPo(account);
                accountPos.add(po);
                resultList.add(account.getAddress().toString());
            }
            if (accountStorageService == null) {
                Log.info("accountStorageService is null");
            }
            accountStorageService.saveAccountList(accountPos);
            LOCAL_ADDRESS_LIST.addAll(resultList);
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
        if (account.isEncrypted() && account.isLocked()) {
            if (!StringUtils.validPassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            try {
                if (!account.decrypt(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        accountStorageService.removeAccount(account.getAddress());
        LOCAL_ADDRESS_LIST.remove(address);
        return Result.getSuccess();
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password) {
        if (null == keyStore || null == keyStore.getAddress()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        //maybe account has been imported
        Account acc = this.getAccountByAddress(keyStore.getAddress());
        if (null != acc) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST);
        }
        Account account;
        byte[] priKey = null;
        if (null != keyStore.getPrikey() && keyStore.getPrikey().length > 0) {
            if (!ECKey.isValidPrivteHex(Hex.encode(keyStore.getPrikey()))) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            priKey = keyStore.getPrikey();
        } else if (null == keyStore.getPrikey() && null != keyStore.getEncryptedPrivateKey() && StringUtils.validPassword(password)) {
            priKey = AESEncrypt.decrypt(Hex.decode(keyStore.getEncryptedPrivateKey()), password);
        } else {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            account = AccountTool.createAccount(Hex.encode(priKey));
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        account.setAddress(new Address(keyStore.getAddress()));
        account.setAlias(keyStore.getAlias());
        account.setPubKey(keyStore.getPubKey());
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        AccountPo po = new AccountPo(account);
        accountStorageService.saveAccount(po);
        LOCAL_ADDRESS_LIST.add(keyStore.getAddress());
        accountLedgerService.importAccountLedger(account.getAddress().getBase58());
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
        Account accountDB = getAccountByAddress(account.getAddress().toString());
        if (null != accountDB) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST);
        }
        if (StringUtils.validPassword(password)) {
            try {
                account.encrypt(password);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        AccountPo po = new AccountPo(account);
        accountStorageService.saveAccount(po);
        LOCAL_ADDRESS_LIST.add(account.getAddress().toString());
        accountLedgerService.importAccountLedger(account.getAddress().getBase58());
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
        if (null != password) {
            if (!StringUtils.validPassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            try {
                if (!account.decrypt(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                account.encrypt(password);
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            EncryptedData encryptedData = new EncryptedData(account.getEncryptedPriKey());
            accountKeyStore.setEncryptedPrivateKey(Hex.encode(encryptedData.getEncryptedBytes()));
        } else {
            if (account.isEncrypted()) {
                //账户已经加密,但是传入密码为空.The account is encrypted, but the pass password is empty.
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
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
        Account accountCache =  accountCacheService.getAccountByAddress(address);
        if(null != accountCache){
            return accountCache;
        }
        AccountPo accountPo = null;
        try {
            accountPo = accountStorageService.getAccount(Base58.decode(address)).getData();
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
        if (accountPo == null) {
            return null;
        }
        Account account = accountPo.toAccount();
        if (!LOCAL_ADDRESS_LIST.contains(account.getAddress().toString())) {
            LOCAL_ADDRESS_LIST.add(account.getAddress().toString());
        }
        return account;
    }

    @Override
    public Result<Account> getAccount(byte[] address) {
        return Result.getSuccess().setData(getAccountByAddress(Base58.encode(address)));
    }

    @Override
    public Result<Account> getAccount(String address) {
        return Result.getSuccess().setData(getAccountByAddress(address));
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
    public Result<List<Account>> getAccountList() {
        List<Account> list = new ArrayList<>();
        List<AccountPo> poList = this.accountStorageService.getAccountList().getData();
        Set<String> addressList = new HashSet<>();
        if (null == poList || poList.isEmpty()) {
            return Result.getSuccess().setData(list);
        }
        for (AccountPo po : poList) {
            Account account = po.toAccount();
            list.add(account);
            addressList.add(account.getAddress().getBase58());
        }
        LOCAL_ADDRESS_LIST = addressList;
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
            return null;
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
            return null;
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
        return new Result(account.isEncrypted(), null);
    }

    @Override
    public Result validPassword(Account account, String password) {
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (null == account) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            if (!account.unlock(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            } else {
                return Result.getSuccess();
            }
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed();
        }
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
        if (null == account.getPriKey() || account.getPriKey().length == 0) {
            throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
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
        if (account.isEncrypted()) {
            AssertUtil.canNotEmpty(password, "password can not be empty");
            return this.signDigest(digest, AESEncrypt.decrypt(account.getEncryptedPriKey(), password));
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
        List<AccountPo> poList = this.accountStorageService.getAccountList().getData();
        if (null == poList || poList.isEmpty()) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        for (AccountPo po : poList) {
            Account account = po.toAccount();
            list.add(account);
        }
        Balance balance = new Balance();
        for (Account account : list) {
            Result<Balance> result = accountLedgerService.getBalance(account.getAddress().getBase58Bytes());
            if (result.isSuccess()) {
                Balance temp = result.getData();
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
}
