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

import io.nuls.account.model.Account;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/4
 */
public class AccountServiceTest {

    private AccountService accountService ;
    @Before
    public void beforeTest(){
        //todo  init accountService
    }

    @Test
    public void createAccount() {
        Result<List<Account>> result = this.accountService.createAccount(0,null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMessage());

        //无密码时
        result = this.accountService.createAccount(1,null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(),1);
        //todo 比较账户和从数据库中查出来的账户是否一致

        result = this.accountService.createAccount(5,null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(),5);
        //todo 比较账户和从数据库中查出来的账户是否一致

        //测试最大一次生成账户数量
        result = this.accountService.createAccount(10000,null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMessage());


        //todo 设置钱包密码
        result = this.accountService.createAccount(1,null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMessage());

        //todo 设置钱包密码为nuls123456
        result = this.accountService.createAccount(1,"nuls123456");
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(),1);
        //todo 比较账户和从数据库中查出来的账户是否一致


        result = this.accountService.createAccount(6,"nuls123456");
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(result.getData().size(),6);
        //todo 比较账户和从数据库中查出来的账户是否一致

        result = this.accountService.createAccount(10000,null);
        assertTrue(result.isFailed());
        assertNotNull(result.getMessage());

    }

    @Test
    public void removeAccount() {
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