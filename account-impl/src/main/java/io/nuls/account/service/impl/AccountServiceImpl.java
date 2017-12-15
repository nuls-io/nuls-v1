package io.nuls.account.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.AccountDao;
import io.nuls.db.entity.AccountPo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountServiceImpl implements AccountService {

    private static AccountServiceImpl thisService = new AccountServiceImpl();

    private Lock locker = new ReentrantLock();

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    private AccountDao accountDao = NulsContext.getInstance().getService(AccountDao.class);

    private boolean isLockNow = true;

    private AccountServiceImpl() {
    }

    public static AccountServiceImpl getInstance() {
        return thisService;
    }

    @Override
    public Account createAccount() {
        locker.lock();
        try {
            Account account = AccountTool.createAccount();
            signAccount(account);
            AccountPo po = new AccountPo();
            AccountTool.toPojo(account, po);
            this.accountDao.save(po);
            this.accountCacheService.putAccount(account);
            return account;
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account failed!");
        } finally {
            locker.unlock();
        }
    }

    @Override
    public Result<List<String>> createAccount(int count) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return new Result<>(false, "Only 0 to 100 can be created at once");
        }

        locker.lock();
        try {
            List<Account> accounts = new ArrayList<>();
            List<AccountPo> accountPos = new ArrayList<>();
            List<String> resultList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                signAccount(account);
                AccountPo po = new AccountPo();
                AccountTool.toPojo(account, po);

                accounts.add(account);
                accountPos.add(po);
                resultList.add(account.getId());
            }

            accountDao.saveBatch(accountPos);
            accountCacheService.putAccountList(accounts);

            return new Result<>(true, "OK", resultList);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account failed!");
        } finally {
            locker.unlock();
        }
    }

    private void signAccount(Account account) {
        if (null == account || account.getEcKey() == null) {
            return;
        }
        //todo
//        byte[] sign = null;
//        //sign by mgprikey
//        Sha256Hash hash = null;
//        try {
//            hash = Sha256Hash.of(account.serialize());
//        } catch (IOException e) {
//            Log.error(e);
//        }
//        ECKey.ECDSASignature signature1 = account.getEcKey().sign(hash);
//        //sign result
//        sign = signature1.encodeToDER();
//        account.setSign(sign);
    }

    @Override
    public void resetKey(Account account) {
        resetKey(account, null);
    }

    @Override
    public void resetKey(Account account, String password) {
        AssertUtil.canNotEmpty(account, "");
        account.resetKey(password);
    }

    @Override
    public void resetKeys() {
        resetKeys(null);
    }

    @Override
    public void resetKeys(String password) {
        List<Account> accounts = this.accountCacheService.getAccountList();
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                account.resetKey(password);
            }
        }
    }

    @Override
    public Account getLocalAccount() {
        return this.accountCacheService.getAccountById(AccountManager.Local_acount_id);
    }

    @Override
    public List<Account> getLocalAccountList() {
        List<Account> list = this.accountCacheService.getAccountList();
        if (null != list && !list.isEmpty()) {
            return list;
        }
        list = new ArrayList<>();
        List<AccountPo> polist = this.accountDao.queryAll();
        if (null == polist || polist.isEmpty()) {
            return list;
        }
        for (AccountPo po : polist) {
            Account account = new Account();
            AccountTool.toBean(po, account);
            list.add(account);
        }
        this.accountCacheService.putAccountList(list);
        return list;
    }

    @Override
    public Account getAccount(String address) {
        AssertUtil.canNotEmpty(address, "");
        return accountCacheService.getAccountByAddress(address);
    }

    @Override
    public Address getAddress(String pubKey) {
        AssertUtil.canNotEmpty(pubKey, "");
        try {
            return AccountTool.newAddress(ECKey.fromPublicOnly(Hex.decode(pubKey)));
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public byte[] getPriKey(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        if (account == null) {
            return null;
        }
        return account.getEcKey().getPrivKeyBytes();
    }

    @Override
    public void switchAccount(String id) {
        Account account = accountCacheService.getAccountById(id);
        if (null != account) {
            AccountManager.Local_acount_id = id;
        } else {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account not exist,id:" + id);
        }
    }

    @Override
    public String getDefaultAccount() {
        return AccountManager.Local_acount_id;
    }

    @Override
    public Result changePassword(String oldpw, String newpw) {
        if (!StringUtils.validPassword(oldpw)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        if (!StringUtils.validPassword(newpw)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        List<Account> accounts = this.getLocalAccountList();
        if (accounts == null || accounts.size() == 0) {
            return new Result(false, "No account was found");
        }

        try {
            Account acct = accounts.get(0);
            if (!acct.isEncrypted()) {
                return new Result(false, "No password has been set up yet");
            }

            List<AccountPo> accountPoList = new ArrayList<>();
            for (Account account : accounts) {
                if (!account.decrypt(oldpw)) {
                    return new Result(false, "old password error");
                }
                account.encrypt(newpw);

                AccountPo po = new AccountPo();
                AccountTool.toPojo(account, po);
                accountPoList.add(po);
            }
            accountDao.updateBatch(accountPoList);
            accountCacheService.putAccountList(accounts);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "change password failed");
        }

        return new Result(true, "OK");
    }

    @Override
    public Result setPassword(String password) {
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }

        List<Account> accounts = this.getLocalAccountList();
        if (accounts == null || accounts.size() == 0) {
            return new Result(false, "No account was found");
        }
        try {
            List<AccountPo> accountPoList = new ArrayList<>();
            for (Account account : accounts) {
                if (account.isEncrypted()) {
                    return new Result(false, "password has been set up");
                } else {
                    account.encrypt(password);
                    AccountPo po = new AccountPo();
                    AccountTool.toPojo(account, po);
                    accountDao.update(po);
                }
            }
            if (accountPoList.size() > 0) {
                accountDao.updateBatch(accountPoList);
            }

            accountCacheService.putAccountList(accounts);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "set password failed");
        }

        return new Result(true, "OK");
    }

    @Override
    public boolean isEncrypted() {
        if (!isLockNow) {
            return false;
        }
        List<Account> accounts = this.getLocalAccountList();
        if (accounts == null || accounts.size() == 0) {
            return false;
        }
        Account account = accounts.get(0);
        return account.isEncrypted();
    }

    @Override
    public Result lockAccounts() {
        return null;
    }

    @Override
    public Result unlockAccounts(String password, int seconds) {
        List<Account> accounts = this.getLocalAccountList();
        if (accounts == null || accounts.isEmpty()) {
            return new Result(false, "No account was found");
        }
        Account acct = accounts.get(0);
        if (!acct.isEncrypted()) {
            return new Result(false, "No password has been set up yet");
        }

        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }

        for (Account account : accounts) {
            if (!account.decrypt(password)) {
                return new Result(false, "password error");
            }
        }

        isLockNow = false;
        long unlockTime = TimeService.currentTimeSeconds() + seconds;
        ThreadManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_ACCOUNT, "unlockAccountThread", new Runnable() {
            @Override
            public void run() {
                while (!isLockNow) {
                    if (TimeService.currentTimeSeconds() > unlockTime) {
                        isLockNow = true;
                        break;
                    } else {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                resetKeys(password);
            }
        });

        return new Result(true, "OK");
    }

    @Override
    public NulsSignData signData(byte[] bytes) {
        return this.signData(bytes, this.getLocalAccount(), null);
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData) {
        return this.signData(digestData, this.getLocalAccount(), null);
    }

    @Override
    public NulsSignData signData(byte[] bytes, String password) {
        return this.signData(bytes, this.getLocalAccount(), password);
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData, String password) {
        return this.signData(digestData, this.getLocalAccount(), password);
    }

    @Override
    public NulsSignData signData(byte[] bytes, Account account, String password) {
        //todo
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        NulsSignData data = new NulsSignData();
        data.setSignBytes(new byte[]{1});
        return data;
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData, Account account, String password) {
        if (null == digestData) {
            return null;
        }
        return this.signData(digestData.getDigestBytes(), account, password);
    }

    @Override
    public void setAlias(String address, String alias) {
        Account account = getAccount(address);
        if (account == null) {
            return;
        }

        if (StringUtils.isNotBlank(account.getAlias())) {
            return;
        }
        if (!StringUtils.validAlias(alias)) {
            return;
        }


    }

    @Override
    public Result verifySign(byte[] bytes, NulsSignData data) {
        return new Result(true, null);
    }
}
