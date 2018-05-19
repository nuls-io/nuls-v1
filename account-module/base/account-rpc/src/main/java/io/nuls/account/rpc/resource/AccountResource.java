package io.nuls.account.rpc.resource;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.model.AccountKeyStore;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.rpc.model.AccountDto;
import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.account.rpc.model.AssetDto;
import io.nuls.account.rpc.model.BalanceDto;
import io.nuls.account.rpc.model.form.*;
import io.nuls.account.service.AccountBaseService;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018/5/13
 */
@Path("/account")
@Api(value = "account", description = "account")
@Component
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AccountBaseService accountBaseService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    private ScheduledExecutorService scheduler;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建账户 [3.3.1]", notes = "result.data: List<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ArrayList.class)
    })
    public Result<List<String>> create(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                               AccountCreateForm form) {
        int count = form.getCount() < 1 ? 1 : form.getCount();
        String password = form.getPassword();
        if (StringUtils.isBlank(password)) {
            password = null;
        }
        Result result = accountService.createAccount(count, password);
        if(result.isFailed()){
            return result;
        }
        List<Account> listAccount = (List<Account>)result.getData();
        List<String> list = new ArrayList<>();
        for (Account account : listAccount) {
            list.add(account.getAddress().toString());
        }
        return Result.getSuccess().setData(list);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户列表 [3.3.4]", notes = "result.data: Page<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountDto.class)
    })
    public Result accountList(@ApiParam(name = "pageNumber", value = "页码")
                              @QueryParam("pageNumber") int pageNumber,
                              @ApiParam(name = "pageSize", value = "每页条数")
                              @QueryParam("pageSize") int pageSize) {
        if (pageNumber < 0 || pageSize < 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 100;
        }
        List<Account> accountList = accountService.getAccountList().getData();
        Page<Account> page = new Page<>(pageNumber, pageSize);
        page.setTotal(accountList.size());
        int start = (pageNumber - 1) * pageSize;
        if (start >= accountList.size()) {
            return Result.getSuccess().setData(page);
        }
        int end = pageNumber * pageSize;
        if (end > accountList.size()) {
            end = accountList.size();
        }
        accountList = accountList.subList(start, end);
        Page<AccountDto> resultPage = new Page<>(page);
        List<AccountDto> dtoList = new ArrayList<>();
        for (Account account : accountList) {
            dtoList.add(new AccountDto(account));
        }
        resultPage.setList(dtoList);
        return Result.getSuccess().setData(resultPage);
    }

    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户信息 [3.3.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountDto.class)
    })
    public Result<AccountDto> get(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return Result.getSuccess().setData(new AccountDto(account));
    }

    @POST
    @Path("/alias/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("设置别名 [3.3.6]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result<Boolean> alias(@PathParam("address") String address,
                                 @ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                         AccountAliasForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(form.getPassword()) && !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return aliasService.setAlias(address, form.getPassword(), form.getAlias());
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户余额 [3.3.3]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BalanceDto.class)
    })
    public Result getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        try {
            Address addr = new Address(address);
            Result<Balance> result = accountLedgerService.getBalance(addr.getBase58Bytes());
            if (result.isFailed()) {
                return result;
            }
            Balance balance = result.getData();
            return Result.getSuccess().setData(new BalanceDto(balance));
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
    }

    @GET
    @Path("/assets/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户资产 [3.3.8]", notes = "result.data: List<AssetDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AssetDto.class)
    })
    public Result getAssets(@ApiParam(name = "address", value = "账户地址", required = true)
                            @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        try {
            Address addr = new Address(address);
            Result<Balance> result = accountLedgerService.getBalance(addr.getBase58Bytes());
            if (result.isFailed()) {
                return result;
            }
            Balance balance = result.getData();
            List<AssetDto> dtoList = new ArrayList<>();
            dtoList.add(new AssetDto("NULS", balance));
            return Result.getSuccess().setData(dtoList);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
    }

    @POST
    @Path("/prikey/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户私钥，只能查询本地创建或导入的账户 [3.3.7]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result getPrikey(@PathParam("address") String address, @ApiParam(name = "form", value = "查询私钥表单数据", required = true)
                                    AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(StringUtils.isNotBlank(form.getPassword()) && !StringUtils.validPassword(form.getPassword())){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return accountBaseService.getPrivateKey(address, form.getPassword());
    }

    @POST
    @Path("/lock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "清除缓存的解锁账户", notes = "Clear the cache unlock account.")
    public Result lock(@ApiParam(name = "address", value = "账户地址", required = true) @PathParam("address") String address) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        accountCacheService.removeAccount(account.getAddress());
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        return Result.getSuccess();
    }

    @POST
    @Path("/unlock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "解锁账户", notes = "")
    public Result unlock(@ApiParam(name = "address", value = "账户地址", required = true)
                         @PathParam("address") String address,
                         @ApiParam(name = "password", value = "账户密码", required = true)
                         @QueryParam("password") String password,
                         @ApiParam(name = "unlockTime", value = "解锁时间默认120秒(单位:秒)")
                         @QueryParam("unlockTime") Integer unlockTime) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            account.unlock(password);
            accountCacheService.putAccount(account);
            if (null == unlockTime || unlockTime > AccountConstant.ACCOUNT_MAX_UNLOCK_TIME) {
                unlockTime = AccountConstant.ACCOUNT_MAX_UNLOCK_TIME;
            }
            if (unlockTime < 0) {
                unlockTime = 0;
            }
            // 一定时间后自动锁定
            scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.schedule(() -> {
                accountCacheService.removeAccount(account.getAddress());
                scheduler.shutdown();
            }, unlockTime, TimeUnit.SECONDS);

        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return Result.getSuccess();
    }


    @POST
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "设置账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result setPassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                     @PathParam("address") String address,
                                 @ApiParam(name = "form", value = "设置钱包密码表单数据", required = true)
                                         AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        String password = form.getPassword();
        if(StringUtils.isBlank(password)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The password is required");
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG,"Length between 8 and 20, the combination of characters and numbers");
        }
        return accountBaseService.setPassword(address, password);
    }

    @PUT
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据原密码修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result updatePassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                              @ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                      AccountUpdatePasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        String password = form.getPassword();
        String newPassword = form.getNewPassword();
        if(StringUtils.isBlank(password)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The password is required");
        }
        if(StringUtils.isBlank(newPassword)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The newPassword is required");
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG,"password Length between 8 and 20, the combination of characters and numbers");

        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG,"newPassword Length between 8 and 20, the combination of characters and numbers");
        }
        return this.accountBaseService.changePassword(address, password, newPassword);
    }

    @PUT
    @Path("/password/prikey/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据私钥修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result updatePasswordByPriKey(@ApiParam(name = "address", value = "账户地址", required = true)
                              @PathParam("address") String address,
                              @ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                      AccountPriKeyPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        String prikey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The prikey is wrong");
        }
        String newPassword = form.getPassword();
        if(StringUtils.isBlank(newPassword)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The newPassword is required");
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG,"Length between 8 and 20, the combination of characters and numbers");
        }
        return this.accountBaseService.changePasswordByPrikey(address, prikey, newPassword);
    }

    @POST
    @Path("/export/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户备份，导出AccountKeyStore字符串 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result<AccountKeyStore> backup(@ApiParam(name = "address", value = "账户地址", required = true)
                                              @PathParam("address") String address,
                                          @ApiParam(name = "form", value = "钱包备份表单数据")
                                                  AccountPasswordForm form) {
        if (StringUtils.isNotBlank(address) && !Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(form.getPassword()) && !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Result<AccountKeyStore> result = accountService.exportAccountToKeyStore(address, form.getPassword());
        if (result.isFailed()) {
            return result;
        }
        AccountKeyStore accountKeyStore = result.getData();
        return Result.getSuccess().setData(new AccountKeyStoreDto(accountKeyStore));
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据AccountKeyStore导入账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result importAccount(@ApiParam(name = "form", value = "导入账户表单数据", required = true)
                                        AccountKeyStoreImportForm form) {

        if (null == form || null == form.getAccountKeyStoreDto()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        AccountKeyStoreDto accountKeyStoreDto = form.getAccountKeyStoreDto();
        String password = form.getPassword();
        if(StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Result result = accountService.importAccountFormKeyStore(accountKeyStoreDto.toAccountKeyStore(), password);
        if (result.isFailed()) {
            return result;
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString());
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据私钥导入账户", notes = "返回账户地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result<String> importAccountByPriKey(@ApiParam(name = "form", value = "导入账户表单数据", required = true)
                                                        AccountPriKeyPasswordForm form) {
        String priKey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Result result = accountService.importAccount(priKey, password);
        if (result.isFailed()) {
            return result;
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString());
    }

    @POST
    @Path("/remove/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "移除账户", notes = "Nuls_RPC_API文档[3.4.9]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result removeAccount(@ApiParam(name = "address", value = "账户地址", required = true)
                                    @PathParam("address") String address,
                                @ApiParam(name = "钱包移除账户表单数据", value = "JSONFormat", required = true)
                                        AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(form.getPassword()) && !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return accountService.removeAccount(address, form.getPassword());
    }
}
