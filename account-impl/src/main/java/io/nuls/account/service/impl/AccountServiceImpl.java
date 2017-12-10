package io.nuls.account.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.manager.AccountManager;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.EncryptedData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.db.dao.AccountDao;
import io.nuls.db.entity.AccountPo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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

    private AccountServiceImpl() {
    }

    public static AccountServiceImpl getInstance() {
        return thisService;
    }

    @Override
    public Account createAccount() {
        locker.lock();
        try {
            ECKey key = new ECKey();
            Address address = new Address(Utils.sha256hash160(key.getPubKey(false)));
            Account account = new Account();
            account.setPriSeed(key.getPrivKeyBytes());
            account.setVersion(new NulsVersion((short) 0));
            account.setAddress(address);
            account.setId(address.toString());
            account.setPubKey(key.getPubKey(true));
            account.setEcKey(key);
            account.setPriKey(key.getPrivKeyBytes());
            account.setCreateTime(TimeService.currentTimeMillis());
            account.setTxHash(new NulsDigestData(new byte[]{0}));
            signAccount(account);
            AccountPo po = new AccountPo();
            AccountTool.toPojo(account, po);
            this.accountDao.save(po);
            this.accountCacheService.putAccount(account);
            return account;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account faild!");
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "create account faild!");
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
    public void resetKey(Account account, String password) {
        AssertUtil.canNotEmpty(account, "");
        byte[] pubkey = account.getPubKey();
        if (!account.isEncrypted()) {
            account.setEcKey(ECKey.fromPrivate(new BigInteger(account.getPriSeed())));
        } else {
            byte[] iv = null;
            if (password == null) {
                iv = Arrays.copyOf(Sha256Hash.hash(pubkey), 16);
            } else {
                iv = Arrays.copyOf(AccountTool.genPrivKey(pubkey, password.getBytes()).toByteArray(), 16);
            }
            //encrypt
            if (account.getEcKey() == null || account.getEcKey().getEncryptedPrivateKey() == null) {
                account.setEcKey(ECKey.fromEncrypted(new EncryptedData(iv, account.getPriSeed()), pubkey));
            } else {
                EncryptedData encryptData = account.getEcKey().getEncryptedPrivateKey();
                encryptData.setInitialisationVector(iv);
                account.setEcKey(ECKey.fromEncrypted(encryptData, pubkey));
            }
        }
    }

    @Override
    public Account getLocalAccount() {
        return this.accountCacheService.getAccountById(AccountManager.Locla_acount_id);
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
        return AccountTool.newAddress(ECKey.fromPublicOnly(Hex.decode(pubKey)));
    }

    @Override
    public byte[] getPriKey(String address) {
        AssertUtil.canNotEmpty(address, "");
        Account account = accountCacheService.getAccountByAddress(address);
        return account.getEcKey().getPrivKeyBytes();
    }

    @Override
    public void switchAccount(String id) {
        Account account = accountCacheService.getAccountById(id);
        if (null != account) {
            AccountManager.Locla_acount_id = id;
        } else {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account not exist,id:" + id);
        }
    }
}
