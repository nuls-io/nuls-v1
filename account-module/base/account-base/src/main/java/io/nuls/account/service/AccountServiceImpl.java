package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.AccountKeyStore;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.tools.crypto.*;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Service
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Autowired
    private AccountStorageService accountStorageService;

    //@Autowired
    //private LedgerService ledgerService;

    @Override
    public Result<List<Account>> createAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return new Result<>(false, "between 0 and 100 can be created at once");
        }
        if (null != password && !StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
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
                AccountPo po = new AccountPo(account);
                accounts.add(account);
                accountPos.add(po);
                resultList.add(account.getAddress().getBase58());
            }

            accountStorageService.saveAccountList(accountPos);
            accountCacheService.putAccountList(accounts);
            AccountConstant.LOCAL_ADDRESS_LIST.addAll(resultList);
            return new Result(true, "OK", resultList);
        } catch (Exception e) {
            Log.error(e);
            //todo remove newaccounts
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
        Account account = getAccountByAddress(address);
        if (account == null) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            if (!account.decrypt(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        accountStorageService.removeAccount(account.getAddress());
        accountCacheService.removeAccount(account.getAddress());
        AccountConstant.LOCAL_ADDRESS_LIST.remove(address);
        if (AccountConstant.DEFAULT_ACCOUNT_ADDRESS != null && AccountConstant.DEFAULT_ACCOUNT_ADDRESS.equals(address)) {
            AccountConstant.DEFAULT_ACCOUNT_ADDRESS = null;
        }
        //to do 等新接口!!!!
        //ledgerService.removeLocalTxs(address);
        return Result.getSuccess();
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password) {

        return null;
    }

    @Override
    public Result<AccountKeyStore> exportAccountToKeyStore(String address, String password) {
        Account account = accountCacheService.getAccountByAddress(address);
        if (account == null) {
            return Result.getFailed(AccountErrorCode.DATA_ERROR);
        }
        try {
            if (!account.decrypt(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            account.encrypt(password);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        EncryptedData encryptedData = new EncryptedData(account.getEncryptedPriKey());
        accountKeyStore.setEncryptedPrivateKey(encryptedData.toString());
        return Result.getSuccess().setData(accountKeyStore);
    }

    @Override
    public Result<Account> getAccount(String address) {
        return Result.getSuccess().setData(getAccountByAddress(address));
    }

    /**
     * 根据账户地址字符串,获取账户对象(内部调用)
     * Get account object based on account address string
     *
     * @param address
     * @return Account
     */
    private Account getAccountByAddress(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        return account;
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
            return Result.getFailed(AccountErrorCode.DATA_ERROR);
        }
        if (!accountCacheService.contains(account.getAddress().getBase58Bytes())) {
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
        Account account = getAccountByAddress(address);
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return new Result(account.isEncrypted(), null);
    }

    @Override
    public Result<Boolean> verifyAddressFormat(String address) {
        return new Result(Address.validAddress(address), null);
    }

    @Override
    public Result<List<Account>> getAccountList() {
        List<Account> list = this.accountCacheService.getAccountList();
        if (null != list && !list.isEmpty()) {
            return Result.getSuccess().setData(list);
        }
        list = new ArrayList<>();
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
        this.accountCacheService.putAccountList(list);
        AccountConstant.LOCAL_ADDRESS_LIST = addressList;
        return Result.getSuccess().setData(list);
    }

    @Override
    public Result<Account> getDefaultAccount() {
        return Result.getSuccess().setData(getDefaultAccountOther());
    }

    /**
     * 获取默认账户账户(内部调用)
     * Get the default account
     *
     * @return Account
     */
    private Account getDefaultAccountOther() {
        Account account = null;
        if (AccountConstant.DEFAULT_ACCOUNT_ADDRESS != null) {
            account = getAccountByAddress(AccountConstant.DEFAULT_ACCOUNT_ADDRESS);
        }
        if (account == null) {
            List<Account> accounts = getAccountList().getData();
            if (accounts != null && !accounts.isEmpty()) {
                account = accounts.get(0);
                AccountConstant.DEFAULT_ACCOUNT_ADDRESS = account.getAddress().getBase58();
            }
        }
        return account;
    }

    @Override
    public NulsSignData signData(byte[] data, Account account, String password) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_ERROR);
        }
        if (account == null) {
            account = getDefaultAccountOther();
            if (account == null) {
                throw new NulsException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), account, password);
    }

    @Override
    public NulsSignData signData(byte[] data, Account account) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_ERROR);
        }
        if (account == null) {
            account = getDefaultAccountOther();
            if (account == null) {
                throw new NulsException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), account.getPriKey());
    }

    @Override
    public NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_ERROR);
        }
        if (null == ecKey) {
            throw new NulsException(AccountErrorCode.DATA_ERROR);
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), ecKey);
    }

    private NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException {
        if (null == digest || digest.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_ERROR);
        }
        if (account.isEncrypted()) {
            return this.signDigest(digest, AESEncrypt.decrypt(account.getEncryptedPriKey(), password));
        } else {
            return this.signDigest(digest, account.getPriKey());
        }
    }

    private NulsSignData signDigest(byte[] digest, byte[] priKey) {
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(priKey));
        return signDigest(digest, ecKey);
    }

    private NulsSignData signDigest(byte[] digest, ECKey ecKey) {
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
    public Result<Balance> getBalance() {
        return null;
    }

    @Override
    public Result<Balance> getBalance(Account account) {
        return null;
    }

    @Override
    public Result<Balance> getBalance(Address address) {
        return null;
    }

    @Override
    public Result<Balance> getBalance(String address) {
        return null;
    }
}
