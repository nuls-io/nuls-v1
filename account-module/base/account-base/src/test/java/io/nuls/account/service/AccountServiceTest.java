/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
 */

package io.nuls.account.service;

import io.nuls.account.model.AccountKeyStore;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.account.model.Account;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;

import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Result;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author: Niels Wang
 */
public class AccountServiceTest {

    protected static AccountService accountService;

    @BeforeClass
    public static void beforeTest() {
        MicroKernelBootstrap kernel = MicroKernelBootstrap.getInstance();
        kernel.init();
        kernel.start();
        LevelDbModuleBootstrap db = new LevelDbModuleBootstrap();
        db.init();
        db.start();
        accountService = SpringLiteContext.getBean(AccountService.class);

    }

    @Test
    public void createAccount() {

        Result<List<Account>> result = this.accountService.createAccount(0, null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMsg());

        //无密码时
        result = this.accountService.createAccount(1, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(), 1);
        //todo 比较账户和从数据库中查出来的账户是否一致

        result = this.accountService.createAccount(5, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(), 5);
        //todo 比较账户和从数据库中查出来的账户是否一致

        //测试最大一次生成账户数量
        result = this.accountService.createAccount(10000, null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMsg());


        //todo 设置钱包密码
        result = this.accountService.createAccount(1, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMsg());

        //todo 设置钱包密码为nuls123456
        result = this.accountService.createAccount(1, "nuls123456");
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(), 1);
        //todo 比较账户和从数据库中查出来的账户是否一致


        result = this.accountService.createAccount(6, "nuls123456");
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(), 6);
        //todo 比较账户和从数据库中查出来的账户是否一致

        result = this.accountService.createAccount(10000, null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMsg());

    }

    @Test
    public void removeAccount() {
        List<Account> accounts = this.accountService.createAccount(2, "nuls123456").getData();
        Result result0 = accountService.removeAccount(accounts.get(0).getAddress().toString(), "nuls123456");
        assertTrue(result0.isSuccess());
        Result result1 = accountService.removeAccount(accounts.get(1).getAddress().toString(), "123456");
        assertTrue(result1.isFailed());
    }

    @Test
    public void getAccount(){
        List<Account> accounts = this.accountService.createAccount(2, "nuls123456").getData();
        Account account = accounts.get(0);
        assertNotNull(accountService.getAccount(account.getAddress()).getData());
        assertEquals(accountService.getAccount(account.getAddress()).getData().getAddress().toString(), account.getAddress().toString());

        Account acc1 = accountService.getAccount(account.getAddress().toString()).getData();
        assertNotNull(acc1);
        assertEquals(acc1.getAddress().toString(), account.getAddress().toString());

        Account acc2 = accountService.getAccount(account.getAddress().getAddressBytes()).getData();
        assertNotNull(acc2);
        assertEquals(acc2.getAddress().toString(), account.getAddress().toString());
    }

    @Test
    public void  getAccountlist(){
        this.accountService.createAccount(50, "nuls123456").getData();
        assertTrue(this.accountService.getAccountList().getData().size()==50);
    }

    @Test
    public void exportAccountToKeyStore(){
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        Result<AccountKeyStore> result = accountService.exportAccountToKeyStore(account.getAddress().toString(), "nuls123456");
        try {
            System.out.println(JSONUtils.obj2PrettyJson(result.getData()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result.getData());

    }

    @Test
    public void importAccount(){
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        accountKeyStore.setAddress("Ns5fRyLX5Z6aNrxSGijUcR9SwjnVivi");
        accountKeyStore.setAlias(null);
        accountKeyStore.setEncryptedPrivateKey("8fd44822ecf4589c02722f2b8f8e8636cd3106c8b85f0fbc87c78bdef64512f7c604e42e3d829fdbe981fb135ed46dc8");
        accountKeyStore.setPrikey(null);
        accountKeyStore.setPubKey(Hex.decode("025e11c5bba00490c15ff9f0c5e24c7141204282fec3ef9b179cc77d947161c4cc"));
        Result<Account> result = accountService.importAccountFormKeyStore(accountKeyStore, "nuls123456");
        assertTrue(result.isSuccess());
        assertNotNull(accountService.getAccount(result.getData().getAddress()));
    }

    @Test
    public void isEncypted(){
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        assertTrue(accountService.isEncrypted(account).isSuccess());
        assertTrue(accountService.isEncrypted(account.getAddress()).isSuccess());
        assertTrue(accountService.isEncrypted(account.getAddress().toString()).isSuccess());

        List<Account> accounts2 = this.accountService.createAccount(1, "").getData();
        Account account2 = accounts2.get(0);
        assertTrue(accountService.isEncrypted(account2).isFailed());
        assertTrue(accountService.isEncrypted(account2.getAddress()).isFailed());
        assertTrue(accountService.isEncrypted(account2.getAddress().toString()).isFailed());
    }

    @Test
    public void validPassword(){
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        assertTrue(accountService.validPassword(account, "nuls123456").isSuccess());
        assertFalse(accountService.validPassword(account, "nuls111111").isSuccess());
        assertFalse(accountService.validPassword(account, "").isSuccess());
    }


    public static void showAccount(Account account) {
        System.out.println("---- account info ----");
        System.out.println("Address： " + account.getAddress().getBase58());
        System.out.println("Public key： " + Hex.encode(account.getPubKey()));
        System.out.println("Private key：" + Hex.encode(account.getPriKey()));
        System.out.println("Encrypted pri key： " + Hex.encode(account.getEncryptedPriKey()));
        System.out.println("key object：");
        System.out.println("\tpublic key：" + account.getEcKey().getPublicKeyAsHex());
        try {
            System.out.println("\tprivate key：" + Hex.encode(account.getEcKey().getPrivKeyBytes()));
        } catch (ECKey.MissingPrivateKeyException e) {
            System.out.println("\tprivate key：is NULL");
        }
        System.out.println("\tencrypted pkey： " + account.getEcKey().getEncryptedPrivateKey());
        System.out.println("---- account info end----");
    }
}