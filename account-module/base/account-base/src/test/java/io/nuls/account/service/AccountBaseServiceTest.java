package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
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
        LevelDbModuleBootstrap db = new LevelDbModuleBootstrap();
        db.init();
        db.start();
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
            assertTrue(acc.decrypt("nuls123456"));
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
            assertFalse(acc.decrypt("nuls123456"));
            assertTrue(acc.decrypt("nuls111111"));
            assertArrayEquals(acc.getPriKey(), account.getPriKey());
        } catch (NulsException e) {

        }
    }

    @Test
    public void changePasswordByPrikey() {
        List<Account> accounts = this.accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        String prik = (String)accountBaseService.getPrivateKey(account.getAddress().toString(), "nuls123456").getData();
        accountBaseService.changePasswordByPrikey(account.getAddress().toString(), prik, "nuls111111");
        Account acc = accountService.getAccount(account.getAddress()).getData();
        try {
            assertTrue(acc.decrypt("nuls111111"));
            assertArrayEquals(acc.getPriKey(), account.getPriKey());
        } catch (NulsException e) {

        }
    }
}