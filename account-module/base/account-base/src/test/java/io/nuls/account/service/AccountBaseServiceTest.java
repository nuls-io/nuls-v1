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
    public void getPrivateKey() {
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
    }

    @Test
    public void setPassword() {
    }

    @Test
    public void changePassword() {
    }

    @Test
    public void changePasswordByPrikey() {
    }
}