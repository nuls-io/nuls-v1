package io.nuls.account.rpc.resources;

import io.nuls.account.service.AccountBaseService;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

/**
 * @author: Charlie
 * @date: 2018/5/14
 */
@Path("/wallet")
@Api(value = "/Wallet", description = "Wallet")
public class WalletResouce {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AccountBaseService accountBaseService;

    @Autowired
    private AccountCacheService accountCacheService;




}
