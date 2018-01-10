package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.entity.validator.AliasValidator;
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
import io.nuls.core.crypto.ECKey;
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
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AccountDataService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.dao.AliasDataService;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.util.TransactionPoTool;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.event.TransactionEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountServiceImpl implements AccountService {

    private static AccountServiceImpl instance = new AccountServiceImpl();

    private Lock locker = new ReentrantLock();

    private AccountCacheService accountCacheService;

    private AccountDataService accountDao;

    private AccountAliasDataService accountAliasDBService;

    private AliasDataService aliasDataService;


    private EventBroadcaster eventBroadcaster;

    private boolean isLockNow = true;

    private String Default_Account_ID;

    private AccountServiceImpl() {

    }

    public static AccountServiceImpl getInstance() {
        return instance;
    }

    @Override
    public void init() {
        accountCacheService = AccountCacheService.getInstance();
        accountDao = NulsContext.getInstance().getService(AccountDataService.class);
        accountAliasDBService = NulsContext.getInstance().getService(AccountAliasDataService.class);
        aliasDataService = NulsContext.getInstance().getService(AliasDataService.class);
        eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

        AliasValidator.getInstance().setAliasDataService(aliasDataService);
        AliasTxService.getInstance().setDataService(accountAliasDBService);
    }

    @Override
    public void start() {
        List<Account> accounts = getAccountList();
        if (accounts != null && !accounts.isEmpty()) {
            Default_Account_ID = accounts.get(0).getId();
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

            accountDao.save(accountPos);
            accountCacheService.putAccountList(accounts);

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
        if (Default_Account_ID == null) {
            return null;
        }
        return getAccount(Default_Account_ID);
    }

    @Override
    public Account getAccount(String address) {
        AssertUtil.canNotEmpty(address, "");
        return accountCacheService.getAccountByAddress(address);
    }

    @Override
    public List<Account> getAccountList() {
        List<Account> list = this.accountCacheService.getAccountList();
        if (null != list && !list.isEmpty()) {
            return list;
        }
        list = new ArrayList<>();
        List<AccountPo> poList = this.accountDao.getList();
        if (null == poList || poList.isEmpty()) {
            return list;
        }
        for (AccountPo po : poList) {
            Account account = new Account();
            AccountTool.toBean(po, account);
            list.add(account);
        }
        this.accountCacheService.putAccountList(list);
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
    public byte[] getPrivateKey(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        if (account == null) {
            return null;
        }
        return account.getEcKey().getPrivKeyBytes();
    }

    @Override
    public void setDefaultAccount(String id) {
        Account account = accountCacheService.getAccountById(id);
        if (null != account) {
            Default_Account_ID = id;
        } else {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account not exist,id:" + id);
        }
        //todo 发送notice给其他模块
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

                //todo
                account.resetKey(oldPassword);
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
    public Result unlockAccounts(String password, int seconds) {
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
        long unlockTime = TimeService.currentTimeSeconds() + seconds;
        TaskManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_ACCOUNT, "unlockAccountThread", new Runnable() {
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
        return this.signData(bytes, this.getDefaultAccount(), null);
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData) {
        return this.signData(digestData, this.getDefaultAccount(), null);
    }

    @Override
    public NulsSignData signData(byte[] bytes, String password) {
        return this.signData(bytes, this.getDefaultAccount(), password);
    }

    @Override
    public NulsSignData signData(NulsDigestData digestData, String password) {
        return this.signData(digestData, this.getDefaultAccount(), password);
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
    public Result verifySign(byte[] bytes, NulsSignData data) {
        //todo
        return new Result(true, null);
    }

//    @Override
//    public Result setAlias(String address, String alias) {
//        try {
//            Result result = canSetAlias(address, alias);
//            if (null == result || result.isFailed()) {
//                return result;
//            }
//            Account account = getAccount(address);
//            result = accountTxDBService.setAlias(address, alias);
//            account.setAlias(alias);
//            accountCacheService.putAccount(account);
//            return result;
//        } catch (Exception e) {
//            return new Result(false, e.getMessage());
//        }
//    }

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
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, e.getMessage());
        }
        return new Result(true, "OK");
    }

    @Override
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
    public Result exportAccount(String address, String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return new Result(false, "filePath is required");
        }
        if (StringUtils.isBlank(address)) {
            return new Result(false, "address is required");
        }
        Account account = getAccount(address);
        if (account == null) {
            return new Result(false, "account not found");
        }

        Result result = backUpFile(filePath);
        if (!result.isSuccess()) {
            return result;
        }
        return exportAccount(account, (File) result.getObject());
    }

    @Override
    public Result exportAccounts(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return new Result(false, "filePath is required");
        }
        List<Account> accounts = getAccountList();
        if (accounts == null || accounts.isEmpty()) {
            return new Result(false, "no account can export");
        }
        if (accounts.size() == 1) {
            return exportAccount(accounts.get(0).getAddress().getBase58(), filePath);
        }
        Result result = backUpFile(filePath);
        if (!result.isSuccess()) {
            return result;
        }
        return exportAccounts(accounts, (File) result.getObject());
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
//todo 服务是否应该提供po的查询接口
//            List<TransactionPo> txList = ledgerService.queryPoListByAccount(account.getAddress().getBase58(), 0, 0);
//            fos.write(new VarInt(txList.size()).encode());
//
//            TransactionPo tx;
//            for (int i = 0; i < txList.size(); i++) {
//                tx = txList.get(i);
//                fos.write(tx.getTxdata());
//            }
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "export failed");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
//todo 服务是否应该提供po的查询接口
//            for (Account account : accounts) {
//                fos.write(account.serialize());
//                txList = ledgerService.queryPoListByAccount(account.getAddress().getBase58(), 0, 0);
//                fos.write(new VarInt(txList.size()).encode());
//
//                for (int i = 0; i < txList.size(); i++) {
//                    tx = txList.get(i);
//                    fos.write(tx.getTxdata());
//                }
//            }
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
            }
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "import failed, file is broken");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new Result(true, "OK");
    }

    private boolean accountExist(Account account) {
        return accountCacheService.accountExist(account.getId());
    }

    private void importSave(List<Account> accounts) throws Exception {
        List<AccountPo> accountPoList = new ArrayList<>();

        for (Account account : accounts) {
            AccountPo accountPo = new AccountPo();
            AccountTool.toPojo(account, accountPo);

            List<TransactionLocalPo> transactionPos = new ArrayList<>();
            for (Transaction tx : account.getMyTxs()) {
                TransactionLocalPo po = TransactionPoTool.toPojoLocal(tx);
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
