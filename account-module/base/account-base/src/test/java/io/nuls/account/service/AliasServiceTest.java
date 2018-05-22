package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.account.model.Alias;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class AliasServiceTest {

    protected static AccountService accountService;

    protected static AccountStorageService accountStorageService;

    protected static AccountCacheService accountCacheService;

    protected static AliasService aliasService;

    protected static AccountBaseService accountBaseService;

    @BeforeClass
    public static void beforeClass(){
        MicroKernelBootstrap kernel = MicroKernelBootstrap.getInstance();
        kernel.init();
        kernel.start();
        LevelDbModuleBootstrap db = new LevelDbModuleBootstrap();
        db.init();
        db.start();
        aliasService = SpringLiteContext.getBean(AliasService.class);
        accountBaseService = SpringLiteContext.getBean(AccountBaseService.class);
        accountService = SpringLiteContext.getBean(AccountService.class);

    }


    @Test
    public void setAlias() {
        List<Account> accounts = accountService.createAccount(1, "nuls123456").getData();
        Account account = accounts.get(0);

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

    @Test
    public void getAlias() {

    }

    @Test
    public void isAliasExist() {

    }

    @Test
    public void rollbackAlias() {

    }
}