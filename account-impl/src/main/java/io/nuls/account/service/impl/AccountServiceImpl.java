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
import io.nuls.account.entity.Alias;
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
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.*;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.script.P2PKHScriptSig;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransferTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
    public void start() {
        List<Account> accounts = getAccountList();
        if (accounts.size() > 0) {
            setDefaultAccount(accounts.get(0).getAddress().getBase58());
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

        //todo need to recover the status of the wallet.
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }

        Account defaultAccount = getDefaultAccount();
        if (defaultAccount != null && defaultAccount.isEncrypted()) {
            try {
                if (!defaultAccount.unlock(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
            } catch (NulsException e) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
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
            if (getDefaultAccount() == null) {
                setDefaultAccount(accounts.get(0).getAddress().getBase58());
            }
            return new Result<>(true, "OK", resultList);
        } catch (Exception e) {
            Log.error(e);
            //todo remove newaccounts
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account failed!");
        } finally {

            locker.unlock();
        }
    }

    @Override
    @DbSession
    public Result removeAccount(String address, String password) {
        Account account = getAccount(address);
        if (account == null) {
            return Result.getFailed(ErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            if (!account.decrypt(password)) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
            }
        } catch (NulsException e) {
            return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
        }
        accountDao.delete(address);
        accountCacheService.removeAccount(address);
        return Result.getSuccess();
    }

    @Override
    public Account getDefaultAccount() {
        Account account = null;
        if (NulsContext.DEFAULT_ACCOUNT_ID != null) {
            account = getAccount(NulsContext.DEFAULT_ACCOUNT_ID);
        }
        if (account == null) {
            List<Account> accounts = getAccountList();
            if (accounts != null && !accounts.isEmpty()) {
                account = accounts.get(0);
                NulsContext.DEFAULT_ACCOUNT_ID = account.getAddress().getBase58();
            }
        }
        return account;
    }

    @Override
    public Account getAccount(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
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
        if (!account.isLocked()) {
            Result result = new Result(true, "OK", Hex.encode(account.getPriKey()));
            return result;
        } else {
            try {
                if (!account.unlock(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
                byte[] publicKeyBytes = account.getPriKey();
                account.lock();
                return new Result(true, "OK", Hex.encode(publicKeyBytes));
            } catch (NulsException e) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
            }
        }
    }

    @Override
    public void setDefaultAccount(String id) {
        if (id == null) {
            return;
        }
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
                    accountPoList.add(po);
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
                if (!account.unlock(oldPassword)) {
                    return new Result(false, "old password error");
                }
                account.encrypt(newPassword, true);

                AccountPo po = new AccountPo();
                AccountTool.toPojo(account, po);
                accountPoList.add(po);
            }

            if (accountPoList.size() > 0) {
                accountDao.update(accountPoList);
            }
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
            try {
                if (!account.unlock(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
            } catch (NulsException e) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
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
                try {
                    resetKeys(password);
                } catch (NulsException e) {
                    Log.error("unlockAccounts resetKey error", e);
                }
                isLockNow = false;
            }
        });
        return new Result(true, "OK");
    }

    @Override
    public NulsSignData signDigest(byte[] digest, byte[] priKey) {
        //todo need to support kinds of algs
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(priKey));
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);
        return nulsSignData;
    }

    @Override
    public NulsSignData signData(byte[] bytes, byte[] priKey) {
        return signDigest(NulsDigestData.calcDigestData(bytes).getDigestBytes(), priKey);
    }

    @Override
    public NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException {
        if (null == digest || digest.length == 0) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
        if (account.isEncrypted()) {
            return this.signDigest(digest, AESEncrypt.decrypt(account.getEncryptedPriKey(), password));
        } else {
            return this.signDigest(digest, account.getPriKey());
        }
    }

    @Override
    public NulsSignData signDigest(NulsDigestData digestData, Account account, String password) throws NulsException {
        if (null == digestData) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
        if (account == null) {
            account = getDefaultAccount();
            if (account == null) {
                throw new NulsException(ErrorCode.ACCOUNT_NOT_EXIST);
            }
        }
        return this.signDigest(digestData.getDigestBytes(), account, password);
    }

    @Override
    public NulsSignData signData(byte[] data, Account account, String password) throws NulsException {
        if (null == data || data.length == 0) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
        if (account == null) {
            account = getDefaultAccount();
            if (account == null) {
                throw new NulsException(ErrorCode.ACCOUNT_NOT_EXIST);
            }
        }
        return this.signDigest(NulsDigestData.calcDigestData(data).getDigestBytes(), account, password);
    }

    @Override
    public P2PKHScriptSig createP2PKHScriptSig(byte[] data, Account account, String password) throws NulsException {
        P2PKHScriptSig p2PKHScriptSig = new P2PKHScriptSig();
        p2PKHScriptSig.setSignData(signData(data, account, password));
        p2PKHScriptSig.setPublicKey(account.getPubKey());
        return p2PKHScriptSig;
    }

    @Override
    public P2PKHScriptSig createP2PKHScriptSigFromDigest(NulsDigestData nulsDigestData, Account account, String password) throws NulsException {
        P2PKHScriptSig p2PKHScriptSig = new P2PKHScriptSig();
        p2PKHScriptSig.setSignData(signDigest(nulsDigestData, account, password));
        p2PKHScriptSig.setPublicKey(account.getPubKey());
        return p2PKHScriptSig;
    }

    @Override
    public Result verifySign(byte[] data, NulsSignData signData, byte[] pubKey) {
        return verifyDigestSign(NulsDigestData.calcDigestData(data), signData, pubKey);
    }

    @Override
    public Result verifyDigestSign(NulsDigestData digestData, NulsSignData signData, byte[] pubKey) {
        ECKey.verify(digestData.getDigestBytes(), signData.getSignBytes(), pubKey);
        //todo
        return new Result(true, null);
    }

    @Override
    public Result setAlias(String address, String password, String alias) {
        Account account = getAccount(address);
        if (account == null) {
            return new Result(false, ErrorCode.ACCOUNT_NOT_EXIST, null);
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return new Result(false, "Alias has been set up");
        }
        if (!StringUtils.validAlias(alias)) {
            return new Result(false, "The alias is between 3 to 20 characters");
        }

        try {
            TransactionEvent event = new TransactionEvent();
            CoinTransferData coinData = new CoinTransferData(OperationType.TRANSFER, AccountConstant.ALIAS_NA, address, null);
            AliasTransaction aliasTx = new AliasTransaction(coinData, password, new Alias(address, alias));
            aliasTx.setHash(NulsDigestData.calcDigestData(aliasTx.serialize()));
            aliasTx.setScriptSig(createP2PKHScriptSigFromDigest(aliasTx.getHash(), account, password).serialize());
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
                try {
                    if (!StringUtils.validPassword(password) || !account.decrypt(password)) {
                        return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                    }
                } catch (NulsException e) {
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
            try {
                account.decrypt(password);
                prikeyList.add(Hex.encode(account.getPriKey()));
                account.encrypt(password);
            } catch (NulsException e) {
                return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("prikeys", prikeyList);
        map.put("password", MD5Util.md5(password));
        return new Result(true, "OK", map);
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
    @DbSession
    public Result importAccount(String priKey, String password) {
        Account account = null;
        try {
            account = AccountTool.createAccount(priKey);
        } catch (NulsException e) {
            return Result.getFailed("invalid prikey");
        }

        //maybe account has been imported
        AccountPo accountPo = accountDao.get(account.getAddress().getBase58());
        if (accountPo != null) {
            return Result.getFailed(ErrorCode.ACCOUNT_EXIST);
        } else {
            accountPo = new AccountPo();
        }

        Account defaultAcct = getDefaultAccount();
        if (defaultAcct != null) {
            try {
                if (!defaultAcct.decrypt(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
                defaultAcct.encrypt(password);
            } catch (NulsException e) {
                e.printStackTrace();
            }
        }
        try {
            account.encrypt(password);
        } catch (NulsException e) {
            e.printStackTrace();
        }

        // save db
        AccountTool.toPojo(account, accountPo);
        AliasPo aliasPo = aliasDataService.getByAddress(accountPo.getAddress());
        if (aliasPo != null) {
            account.setAlias(aliasPo.getAlias());
            accountPo.setAlias(aliasPo.getAlias());
        }
        accountDao.save(accountPo);
        ledgerService.saveTxInLocal(accountPo.getAddress());

        // save cache
        accountCacheService.putAccount(account);
        NulsContext.LOCAL_ADDRESS_LIST.add(accountPo.getAddress());
        ledgerService.getBalance(accountPo.getAddress());
        AccountImportedNotice notice = new AccountImportedNotice();
        notice.setEventBody(account);
        eventBroadcaster.publishToLocal(notice);
        Result result = Result.getSuccess();
        result.setObject(accountPo.getAddress());
        return result;
    }

    @Override
    @DbSession
    public Result importAccounts(List<String> keys, String password) {
        Account account = null;
        AccountPo accountPo = null;
        AliasPo aliasPo = null;
        Account defaultAcct = getDefaultAccount();
        if (defaultAcct != null) {
            try {
                if (!defaultAcct.decrypt(password)) {
                    return Result.getFailed(ErrorCode.PASSWORD_IS_WRONG);
                }
                defaultAcct.encrypt(password);
            } catch (NulsException e) {
            }
        }

        for (String priKey : keys) {
            try {
                account = AccountTool.createAccount(priKey);
                account.encrypt(password);
            } catch (NulsException e) {
                return Result.getFailed("invalid prikey");
            }
            accountPo = accountDao.get(account.getAddress().getBase58());
            if (accountPo != null) {
                continue;
            } else {
                accountPo = new AccountPo();
            }
            // save db
            AccountTool.toPojo(account, accountPo);
            aliasPo = aliasDataService.getByAddress(accountPo.getAddress());
            if (aliasPo != null) {
                account.setAlias(aliasPo.getAlias());
                accountPo.setAlias(aliasPo.getAlias());
            }
            accountDao.save(accountPo);
            ledgerService.saveTxInLocal(accountPo.getAddress());

            // save cache
            accountCacheService.putAccount(account);
            NulsContext.LOCAL_ADDRESS_LIST.add(accountPo.getAddress());
            ledgerService.getBalance(accountPo.getAddress());
        }
        return Result.getSuccess();
    }

    @Override
    public Alias getAlias(String address) {
        AliasPo aliasPo = aliasDataService.getByAddress(address);
        if (aliasPo == null) {
            return null;
        }
        Alias alias = new Alias(aliasPo.getAddress(), aliasPo.getAlias());
        return alias;
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

    private void resetKeys(String password) throws NulsException {
        List<Account> accounts = this.getAccountList();
        for (Account account : accounts) {
            account.encrypt(password);
        }
    }

    private void signAccount(Account account) {
        if (null == account || account.getEcKey() == null) {
            return;
        }
        //todo
    }
}
