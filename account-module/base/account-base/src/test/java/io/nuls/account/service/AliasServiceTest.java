package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Result;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AliasServiceTest {

    protected static AccountService accountService;
    protected static AliasService aliasService;

    @Before
    public void beforeClass(){
        MicroKernelBootstrap kernel = MicroKernelBootstrap.getInstance();
        kernel.init();
        kernel.start();
        LevelDbModuleBootstrap db = new LevelDbModuleBootstrap();
        db.init();
        db.start();
        accountService = SpringLiteContext.getBean(AccountService.class);
        aliasService = SpringLiteContext.getBean(AliasService.class);
    }

    @Test
    public void setAlias() {
        List<Account> accounts = accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        Result result = aliasService.setAlias(account.getAddress().toString(), "nuls123456", "Charlie555");
        assertTrue(result.isSuccess());
    }

    @Test
    public void saveAlias() {
        List<Account> accounts = accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);
        Alias alias = new Alias(account.getAddress().getBase58Bytes(), "lichao");
        try {
            assertTrue(aliasService.saveAlias(new AliasPo(alias)).isSuccess());
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
}