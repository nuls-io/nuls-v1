package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.EncryptedData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Niels on 2017/10/30.
 * nuls.io
 */
public class AccountServiceImpl implements AccountService {

    private static AccountServiceImpl thisService = new AccountServiceImpl();

    private Lock locker = new ReentrantLock();

    private CacheService<Account> cacheService;


    private AccountServiceImpl() {
        NulsContext context = NulsContext.getInstance();
        this.cacheService = context.getService(CacheService.class);
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
            account.setVersion(AccountConstant.ACCOUNT_MODULE_VERSION);
            account.setAddress(address);
            account.setPubKey(key.getPubKey(true));
            signAccount(account);
            //todo save account to database（local）
            account.setEcKey(key);
            this.cacheService.putElement(AccountConstant.ACCOUNT_LIST_CACHE, account.getAddress().toString(), account);
            return account;
        } finally {
            locker.unlock();
        }
    }

    private void signAccount(Account account) {
        if (null == account || account.getEcKey() == null) {
            return;
        }
        byte[] sign = null;
        //sign by mgprikey
        Sha256Hash hash = null;
        try {
            hash = Sha256Hash.of(account.serialize());
        } catch (IOException e) {
            Log.error(e);
        } catch (NulsException e) {
            Log.error(e);
        }
        ECKey.ECDSASignature signature1 = account.getEcKey().sign(hash);
        //sign result
        sign = signature1.encodeToDER();
        ((Account) account).setSign(sign);
    }

    @Override
    public void resetKey(Account account, String password) {
        AssertUtil.canNotEmpty(account,"");
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
        //todo
        return null;
    }

    @Override
    public List<Account> getLocalAccountList() {
        //todo
        return null;
    }

    @Override
    public Account getAccount(String address) {
        AssertUtil.canNotEmpty(address,"");
        return cacheService.getElementValue(AccountConstant.ACCOUNT_LIST_CACHE, address);
    }

    @Override
    public double getAccountCredit(String address) {
        AssertUtil.canNotEmpty(address,"");
        Account account = cacheService.getElementValue(AccountConstant.ACCOUNT_LIST_CACHE, address);
        return account.getCredit();
    }

    @Override
    public Address getAddress(String pubKey) {
        AssertUtil.canNotEmpty(pubKey,"");
        return AccountTool.newAddress(ECKey.fromPublicOnly(Hex.decode(pubKey)));
    }

    @Override
    public byte[] getPriKey(String address) {
        AssertUtil.canNotEmpty(address,"");
        Account account = cacheService.getElementValue(AccountConstant.ACCOUNT_LIST_CACHE, address);
        return account.getEcKey().getPrivKeyBytes();
    }
}
