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
 *
 */

package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Result;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AccountBaseServiceTest {

    protected static AccountService accountService;
    protected static AccountBaseService accountBaseService;
    @BeforeClass
    public static void beforeTest() {
        MicroKernelBootstrap kernel = MicroKernelBootstrap.getInstance();
        kernel.init();
        kernel.start();
        accountService = SpringLiteContext.getBean(AccountService.class);
        accountBaseService = SpringLiteContext.getBean(AccountBaseService.class);

    }

    @Test
    public void getPrivateKeyTest() {
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        Result result = accountBaseService.getPrivateKey(account.getAddress().toString(), "nuls123456");
        assertTrue(result.isSuccess());
        try {
            account.unlock("nuls123456");
        } catch (NulsException e) {
            e.printStackTrace();
        }
        assertArrayEquals(Hex.decode((String)result.getData()), account.getPriKey());

        List<Account> accounts2 = this.accountService.createAccount(1, "").getData();
        Account account2 = accounts2.get(0);
        Result result2 = accountBaseService.getPrivateKey(account2.getAddress().toString(), "");
        assertTrue(result2.isSuccess());
        assertArrayEquals(Hex.decode((String)result2.getData()), account2.getPriKey());
    }

    @Test
    public void setPassword() {
        List<Account> accounts = this.accountService.createAccount(1, "").getData();
        Account account = accounts.get(0);
        accountBaseService.setPassword(account.getAddress().toString(),"nuls123456");
        Account acc = accountService.getAccount(account.getAddress()).getData();
        try {
            assertTrue(acc.unlock("nuls123456"));
            assertArrayEquals(acc.getPriKey(), account.getPriKey());
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changePassword() {
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        accountBaseService.changePassword(account.getAddress().toString(),"nuls123456", "nuls111111");
        Account acc = accountService.getAccount(account.getAddress()).getData();
        try {
            assertFalse(acc.unlock("nuls123456"));
            assertTrue(acc.unlock("nuls111111"));
            assertArrayEquals(acc.getPriKey(), account.getPriKey());
        } catch (NulsException e) {

        }
    }

}
