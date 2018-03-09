/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.account.event.*;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.service.tx.AliasTxService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.AESEncrypt;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.MD5Util;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransferTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();
    @Autowired
    private AccountDataService accountDao;
    @Autowired
    private AccountAliasDataService accountAliasDBService;
    @Autowired
    private AliasDataService aliasDataService;
    @Autowired
    private EventBroadcaster eventBroadcaster;
    @Autowired
    private LedgerService ledgerService;

    private volatile boolean isLockNow = true;

    @Override
    public void init() {
        AliasValidator.getInstance().setAliasDataService(aliasDataService);
        AliasTxService.getInstance().setDataService(accountAliasDBService);
    }

    @Override
    public void start() {
        List<Account> accounts = getAccountList();
        Set<String> addressList = new HashSet<>();
        if (accounts != null && !accounts.isEmpty()) {
            NulsContext.DEFAULT_ACCOUNT_ID = accounts.get(0).getAddress().getBase58();
            for (Account account : accounts) {
                addressList.add(account.getAddress().getBase58());
            }
            NulsContext.LOCAL_ADDRESS_LIST = addressList;
        }
    }

    @Override
    public void shutdown() {
        accountCacheService.clear();
    }

    @Override
    public void destroy() {
        accountCacheService.destroy();
    }

    @Override
    @DbSession
    public Account createAccount(String passwd) {
        locker.lock();
        try {
            Account account = AccountTool.createAccount();
            account.encrypt(passwd);
            signAccount(account);
            AccountPo po = new AccountPo();
            AccountTool.toPojo(account, po);
            this.accountDao.save(po);
            this.accountCacheService.putAccount(account);
            NulsContext.LOCAL_ADDRESS_LIST.add(account.getAddress().getBase58());
            AccountCreateNotice notice = new AccountCreateNotice();
            notice.setEventBody(account);
            eventBroadcaster.publishToLocal(notice);
            return account;
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account failed!");
        } finally {
            locker.unlock();
        }
    }

    @Override
    @DbSession
    public Result<List<String>> createAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return new Result<>(false, "between 0 and 100 can be created at once");
        }

        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }

        Account defaultAccount = getDefaultAccount();
        if (defaultAccount != null) {
            defaultAccount.decrypt(password);
            if (defaultAccount.isEncrypted()) {
                return new Result(false, "incorrect password");
            }
        }

        locker.lock();
        try {
            List<Account> accounts = new ArrayList<>();
            List<AccountPo> accountPos = new ArrayList<>();
            List<String> resultList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                signAccount(account);
                account.encrypt(password);
                AccountPo po = new AccountPo();
                AccountTool.toPojo(account, po);

                accounts.add(account);
                accountPos.add(po);
                resultList.add(account.getAddress().getBase58());
            }

            accountDao.save(accountPos);
            accountCacheService.putAccountList(accounts);
            NulsContext.LOCAL_ADDRESS_LIST.addAll(resultList);
            for (Account account : accounts) {
                AccountCreateNotice notice = new AccountCreateNotice();
                notice.setEventBody(account);
                eventBroadcaster.publishToLocal(notice);
            }
            return new Result<>(true, "OK", resultList);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account failed!");
        } finally {
            locker.unlock();
        }
    }


    @Override
    public Account getDefaultAccount() {
        if (NulsContext.DEFAULT_ACCOUNT_ID == null) {
            return null;
        }
        return getAccount(NulsContext.DEFAULT_ACCOUNT_ID);
    }

    @Override
    public Account getAccount(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        if (account == null) {
            AliasPo aliasPo = aliasDataService.getByAddress(address);
            if (aliasPo != null) {
                try {
                    account = new Account();
                    account.setAddress(Address.fromHashs(address));
                    account.setAlias(aliasPo.getAlias());
                } catch (NulsException e) {
                    e.printStackTrace();
                }
            }
        }
        return account;
    }

    @Override
    public boolean isMine(String address) {
        return accountCacheService.contains(address);
    }

    @Override
    public List<Account> getAccountList() {
        List<Account> list = this.accountCacheService.getAccountList();
        if (null != list && !list.isEmpty()) {
            return list;
        }
        list = new ArrayList<>();
        List<AccountPo> poList = this.accountDao.getList();
        Set<String> addressList = new HashSet<>();
        if (null == poList || poList.isEmpty()) {
            return list;
        }
        for (AccountPo po : poList) {
            Account account = new Account();
            AccountTool.toBean(po, account);
            list.add(account);
            addressList.add(account.getAddress().getBase58());
        }
        this.accountCacheService.putAccountList(list);
        NulsContext.LOCAL_ADDRESS_LIST = addressList;
        return list;
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
    public Result getPrivateKey(String address, String password) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        if (account == null) {
            return Result.getFailed(ErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (!account.isEncrypted()) {
            Result result = new Result(true, "OK", Hex.encode(account.getPriKey()));
            return result;
        } else {
            account.unlock(password);
            if (account.isEncrypted()) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
            }
            byte[] publicKeyBytes = account.getPriKey();
            account.lock();
            return new Result(true, "OK", Hex.encode(publicKeyBytes));
        }
    }

    @Override
    public void setDefaultAccount(String id) {
        Account account = accountCacheService.getAccountById(id);
        if (null != account) {
            NulsContext.DEFAULT_ACCOUNT_ID = id;
        } else {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account not exist,id:" + id);
        }
        DefaultAccountChangeNotice notice = new DefaultAccountChangeNotice();
        notice.setEventBody(account);
        eventBroadcaster.publishToLocal(notice);
    }

    @Override
    public Result encryptAccount(String password) {
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }

        List<Account> accounts = this.getAccountList();
        if (accounts == null || accounts.isEmpty()) {
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
                accountDao.update(accountPoList);
            }

            accountCacheService.putAccountList(accounts);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "set password failed");
        }

        return new Result(true, "OK");
    }

    @Override
    public Result changePassword(String oldPassword, String newPassword) {
        if (!StringUtils.validPassword(oldPassword)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        if (!StringUtils.validPassword(newPassword)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        List<Account> accounts = this.getAccountList();
        if (accounts == null || accounts.isEmpty()) {
            return new Result(false, "No account was found");
        }

        try {
            Account acct = accounts.get(0);
            if (!acct.isEncrypted()) {
                return new Result(false, "No password has been set up yet");
            }

            List<AccountPo> accountPoList = new ArrayList<>();
            for (Account account : accounts) {
                if (!account.decrypt(oldPassword)) {
                    return new Result(false, "old password error");
                }

                account.decrypt(oldPassword);
                account.encrypt(newPassword);

                AccountPo po = new AccountPo();
                AccountTool.toPojo(account, po);
                accountPoList.add(po);
            }
            accountDao.update(accountPoList);
            accountCacheService.putAccountList(accounts);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "change password failed");
        }
        this.eventBroadcaster.publishToLocal(new PasswordChangeNotice());
        return new Result(true, "OK");
    }

    @Override
    public boolean isEncrypted() {
        if (!isLockNow) {
            return false;
        }
        List<Account> accounts = this.getAccountList();
        if (accounts == null || accounts.size() == 0) {
            return false;
        }
        Account account = accounts.get(0);
        return account.isEncrypted();
    }

    @Override
    public Result unlockAccounts(final String password, int seconds) {
        List<Account> accounts = this.getAccountList();
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
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_ACCOUNT, "unlockAccountThread", new Runnable() {
            @Override
            public void run() {
                isLockNow = true;
                try {
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                resetKeys(password);
                isLockNow = false;
            }
        });
        return new Result(true, "OK");
    }

    @Override
    public NulsSignData signData(byte[] bytes, String password) {
        return this.signData(bytes, this.getDefaultAccount(), password);
    }

    @Override
    public NulsSignData signData(byte[] bytes, byte[] priKey) {
        //todo
        return NulsSignData.EMPTY_SIGN;
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData, String password) {
        return this.signData(digestData, this.getDefaultAccount(), password);
    }

    @Override
    public NulsSignData signData(byte[] bytes, Account account, String password) {
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        if (account.isEncrypted()) {
            return this.signData(bytes, AESEncrypt.decrypt(account.getEncryptedPriKey(), password));
        } else {
            return this.signData(bytes, account.getPriKey());
        }
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData, Account account, String password) {
        if (null == digestData) {
            return null;
        }
        return this.signData(digestData.getWholeBytes(), account, password);
    }

    @Override
    public Result verifySign(byte[] bytes, NulsSignData data) {
        //todo
        return new Result(true, null);
    }

    @Override
    public Result setAlias(String address, String password, String alias) {
        Account account = getAccount(address);
        if (account == null) {
            return new Result(false, "Account not found");
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return new Result(false, "Alias has been set up");
        }
        if (!StringUtils.validAlias(alias)) {
            return new Result(false, "The alias is between 3 to 20 characters");
        }

        try {
            TransactionEvent event = new TransactionEvent();
            CoinTransferData coinData = new CoinTransferData(AccountConstant.ALIAS_NA, address, null);
            AliasTransaction aliasTx = new AliasTransaction(coinData, password);
            aliasTx.setHash(NulsDigestData.calcDigestData(aliasTx.serialize()));
            aliasTx.setSign(signData(aliasTx.getHash(), account, password));
            ValidateResult validate = aliasTx.verify();
            if (validate.isFailed()) {
                return new Result(false, validate.getMessage());
            }

            event.setEventBody(aliasTx);
            eventBroadcaster.broadcastAndCache(event, true);
            SetAliasNotice notice = new SetAliasNotice();
            notice.setEventBody(aliasTx);
            eventBroadcaster.publishToLocal(notice);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, e.getMessage());
        }
        return new Result(true, "OK");
    }

    //    @Override
    public Result exportAccount(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return new Result(false, "filePath is required");
        }
        Account account = getDefaultAccount();
        if (account == null) {
            return new Result(false, "no account can export");
        }
        Result result = backUpFile(filePath);
        if (!result.isSuccess()) {
            return result;
        }
        return exportAccount(account, (File) result.getObject());
    }

    @Override
    public Result exportAccount(String address, String password) {
        Account account = null;
        if (!StringUtils.isBlank(address)) {
            account = accountCacheService.getAccountByAddress(address);
            if (account == null) {
                return Result.getFailed(ErrorCode.DATA_NOT_FOUND);
            }
            if (account.isEncrypted()) {
                if (!StringUtils.validPassword(password) || !account.decrypt(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
            }
        }

        Result result = backUpFile("");
        if (!result.isSuccess()) {
            return result;
        }
        return exportAccount(account, (File) result.getObject());
    }

    @Override
    public Result exportAccounts(String password) {
        List<Account> accounts = accountCacheService.getAccountList();
        if (null == accounts || accounts.isEmpty()) {
            return Result.getFailed("no account can export");
        }
        List<String> prikeyList = new ArrayList<>();
        for (Account account : accounts) {
            account.decrypt(password);
            if (account.isEncrypted()) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
            }
            prikeyList.add(Hex.encode(account.getPriKey()));
            account.encrypt(password);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("prikeys", prikeyList);
        map.put("password", MD5Util.md5(password));
        return new Result(true, "OK", prikeyList);
    }


    private Result<File> backUpFile(String filePath) {
        File backupFile = new File(filePath);
        //Does the superior directory exist
        if (!backupFile.getParentFile().exists() && !backupFile.getParentFile().mkdirs()) {
            return new Result(false, "create parent directory failed");
        }
        //if isDirectory(), rename backupFile
        if (backupFile.isDirectory()) {
            if (!backupFile.exists() && !backupFile.mkdir()) {
                return new Result<>(false, "create directory failed");
            }
            backupFile = new File(backupFile, "wallet_backup_".concat(DateUtil.convertDate(new Date(TimeService.currentTimeMillis()), "yyyyMMddHHmm")).concat(".dat"));
        }
        //create backupFile
        try {
            if (!backupFile.exists() && !backupFile.createNewFile()) {
                return new Result<>(false, "create file failed");
            }
        } catch (IOException e) {
            Log.error(e);
            return new Result(false, "create file failed");
        }

        return new Result<>(true, "OK", backupFile);
    }

    private Result exportAccount(Account account, File backupFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(backupFile);
            fos.write(1);   //account length
            fos.write(account.serialize());

        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "export failed");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return new Result(true, "OK");
    }

    private Result exportAccounts(List<Account> accounts, File backupFile) {
        FileOutputStream fos = null;
        List<TransactionPo> txList;
        TransactionPo tx;
        try {
            fos = new FileOutputStream(backupFile);
            fos.write(new VarInt(accounts.size()).encode());   //account length
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "export failed");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {

                }
            }
        }
        return new Result(true, "OK");
    }

    @Override
    public Result importAccount(String priKey) {
        String address = new ECKey().getPublicKeyAsHex(true);

        AccountPo accountPo = accountDao.get(address);
        if (accountPo != null) {
            return Result.getSuccess();
        }
        ledgerService.saveTxInLocal(address);
        AliasPo aliasPo = aliasDataService.getByAddress(address);
        if (aliasPo != null) {
            accountPo.setAlias(aliasPo.getAlias());
            accountDao.updateAlias(accountPo);
        }

        ledgerService.getBalance(address);
        Account account = new Account();
        AccountTool.toBean(accountPo, account);
        accountCacheService.putAccount(account);
        NulsContext.LOCAL_ADDRESS_LIST.add(address);
        AccountImportedNotice notice = new AccountImportedNotice();
        notice.setEventBody(account);
        eventBroadcaster.publishToLocal(notice);
        return Result.getSuccess();
    }

    //    @Override
    public Result importAccountsFile(String walletFilePath) {
        if (StringUtils.isBlank(walletFilePath)) {
            return new Result(false, "walletFilePath is required");
        }
        File walletFile = new File(walletFilePath);
        if (!walletFile.exists()) {
            return new Result(false, "file not found");
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(walletFile);
            byte[] datas = new byte[fis.available()];
            fis.read(datas);

            NulsByteBuffer buffer = new NulsByteBuffer(datas);
            int accountSize = (int) buffer.readVarInt();
            List<Account> accounts = new ArrayList<>();

            for (int i = 0; i < accountSize; i++) {
                Account account = new Account(buffer);
                if (accountExist(account)) {
                    continue;
                }
                int txSize = (int) buffer.readVarInt();
                List<Transaction> txList = new ArrayList<>();
                for (int j = 0; j < txSize; j++) {
                    Transaction tx = TransactionManager.getInstance(buffer);
                    txList.add(tx);
                }
                account.setMyTxs(txList);
                accounts.add(account);
            }

            //save database
            importSave(accounts);

            for (Account account : accounts) {
                accountCacheService.putAccount(account);
                AccountImportedNotice notice = new AccountImportedNotice();
                notice.setEventBody(account);
                eventBroadcaster.publishToLocal(notice);
            }
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "import failed, file is broken");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return new Result(true, "OK");
    }

    private boolean accountExist(Account account) {
        return accountCacheService.accountExist(account.getAddress().getBase58());
    }

    private void importSave(List<Account> accounts) throws Exception {
        List<AccountPo> accountPoList = new ArrayList<>();

        for (Account account : accounts) {
            AccountPo accountPo = new AccountPo();
            AccountTool.toPojo(account, accountPo);

            List<TransactionLocalPo> transactionPos = new ArrayList<>();
            for (Transaction tx : account.getMyTxs()) {
                TransactionLocalPo po = UtxoTransferTool.toLocalTransactionPojo(tx);
                transactionPos.add(po);
            }
            accountPo.setMyTxs(transactionPos);
            accountPoList.add(accountPo);
        }
        accountAliasDBService.importAccount(accountPoList);
    }

    private void resetKeys(String password) {
        //todo
    }

    private void signAccount(Account account) {
        if (null == account || account.getEcKey() == null) {
            return;
        }
        //todo
    }
}
