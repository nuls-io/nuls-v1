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
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        Account defaultAccount = getDefaultAccount().getData();
        if (defaultAccount != null && defaultAccount.isEncrypted()) {
            try {
                if (!defaultAccount.unlock(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                defaultAccount.lock();
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }

        locker.lock();
        try {
            List<Account> accounts = new ArrayList<>();
            List<AccountPo> accountPos = new ArrayList<>();
            List<String> resultList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                account.encrypt(password);
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
    public Result<Boolean> removeAccount(String address, String password) {
        Account account = getAccountPrivate(address);
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
        return null;
    }

    @Override
    public Result<Account> getAccount(String address) {
        return Result.getSuccess().setData(getAccountPrivate(address));
    }

    /**
     * 根据账户地址字符串,获取账户对象(内部调用)
     * Get account object based on account address string
     * @param address
     * @return Account
     */
    private Account getAccountPrivate(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        return account;
    }

    @Override
    public Result<Account> getAccount(Address address) {
        if (null == address ) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountPrivate(address.toString());
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
        if(null == account || null == account.getAddress()){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if(!accountCacheService.contains(account.getAddress().getBase58Bytes())){
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
        Account account = getAccountPrivate(address);
        if(null == account){
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
        return Result.getSuccess().setData(getDefaultAccountPrivate());
    }
    private Account getDefaultAccountPrivate() {
        Account account = null;
        if (AccountConstant.DEFAULT_ACCOUNT_ADDRESS != null) {
            account = getAccountPrivate(AccountConstant.DEFAULT_ACCOUNT_ADDRESS);
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
        return null;
    }

    @Override
    public NulsSignData signData(byte[] data, Account account) throws NulsException {
        return null;
    }

    @Override
    public NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException {
        return null;
    }

    @Override
    public Result verifySignData(byte[] data, NulsSignData signData, byte[] pubKey) {
        return null;
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
