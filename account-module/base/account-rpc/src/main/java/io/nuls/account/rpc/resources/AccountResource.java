package io.nuls.account.rpc.resources;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
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
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
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

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

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
        if (null == form.getPassword() || "".equals(form.getPassword())) {
            password = null;
        }
        List<Account> listAccount = accountService.createAccount(count, password).getData();
        List<String> list = new ArrayList<>();
        for (Account account : listAccount) {
            list.add(account.getAddress().toString());
        }
        return Result.getSuccess().setData(list);
    }

    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户信息 [3.3.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountDto.class)
    })
    public Result<AccountDto> get(@ApiParam(name = "address", value = "账户地址 ，缺省时默认为所有账户", required = true)
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
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("设置别名 [3.3.6]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result<Boolean> alias(@ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                         AccountAliasForm form) {
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(form.getAlias()) || !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return aliasService.setAlias(form.getAddress(), form.getPassword(), form.getAlias());
    }

    @GET
    @Path("/list")
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
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户余额 [3.3.3]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BalanceDto.class)
    })
    public Result<BalanceDto> getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Balance balance = null;
        try {
            balance = accountService.getBalance(address).getData();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        if (balance == null) {
            balance = new Balance();
        }
        Result result = Result.getSuccess();
        result.setData(new BalanceDto(balance));
        return result;
    }

    @GET
    @Path("/balances")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询当前钱包所有账户余额(合计) [3.3.9]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BalanceDto.class)
    })
    public Result getBalance() {
        Balance balance = null;
        try {
            balance = accountService.getBalance().getData();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        Result result = Result.getSuccess();
        result.setData(new BalanceDto(balance));
        return result;
    }


    @POST
    @Path("/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户私钥，只能查询本地创建或导入的账户 [3.3.7]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result getPrikey(@ApiParam(name = "form", value = "查询私钥表单数据", required = true)
                                    AccountAPForm form) {
        if (!Address.validAddress(form.getAddress()) || !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        return accountBaseService.getPrivateKey(form.getAddress(), form.getPassword());
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

        Balance balance = null;
        try {
            balance = accountService.getBalance(address).getData();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        Result result = Result.getSuccess();
        List<AssetDto> dtoList = new ArrayList<>();
        dtoList.add(new AssetDto("NULS", balance));
        result.setData(dtoList);
        return result;
    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取账户地址", notes = "result.data: String")
    public Result getAddress(@QueryParam("publicKey") String publicKey,
                             @QueryParam("subChainId") Integer subChainId) {
        if (subChainId < 1 || subChainId >= 65535) {
            return Result.getFailed(KernelErrorCode.CHAIN_ID_ERROR);
        }
        Address address = new Address((short) subChainId.intValue(), Hex.decode(publicKey));
        Result result = Result.getSuccess();
        result.setData(address.toString());
        return result;
    }

    @DELETE
    @Path("/cache")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "清除缓存的解锁账户", notes = "Clear the cache unlock account.")
    public Result clearCacheOfUnlockAccount(@QueryParam("address") String address) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        accountCacheService.removeAccount(account.getAddress());
        return Result.getSuccess();
    }

    @POST
    @Path("/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "解锁账户", notes = "")
    public Result unlock(@QueryParam("address") String address, @QueryParam("password") String password, @QueryParam("unlockTime") Integer unlockTime) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            account.decrypt(password);
            accountCacheService.putAccount(account);
            if (null == unlockTime || unlockTime > AccountConstant.ACCOUNT_MAX_UNLOCK_TIME) {
                unlockTime = AccountConstant.ACCOUNT_MAX_UNLOCK_TIME;
            }
            if (unlockTime < 0) {
                unlockTime = 0;
            }
            // 一定时间后自动锁定
            ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
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
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "设置账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result updatePassword(@ApiParam(name = "form", value = "设置钱包密码表单数据", required = true)
                                             AccountSetPasswordForm form) {

        String address = form.getAddress();
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        String password = form.getPassword();
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        return accountBaseService.setPassword(address, password);
    }

    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public Result setPassword(@ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                      AccountPasswordForm form) {
        String address = form.getAddress();
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        String password = form.getPassword();
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        String newPassword = form.getNewPassword();
        if (!StringUtils.validPassword(newPassword)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        return this.accountBaseService.changePassword(address, password, newPassword);
    }

    @POST
    @Path("/export")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户备份，导出AccountKeyStore字符串 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Result.class)
    })
    public Result<String> backup(@ApiParam(name = "form", value = "钱包备份表单数据")
                                    AccountAPForm form) {
        if (StringUtils.isNotBlank(form.getAddress()) && !Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (null != form.getPassword() && !StringUtils.validPassword(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        AccountKeyStore accountKeyStore = accountService.exportAccountToKeyStore(form.getAddress(), form.getPassword()).getData();
        return Result.getSuccess().setData(new AccountKeyStoreDto(accountKeyStore));
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据AccountKeyStore导入账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Result.class)
    })
    public Result importAccount(@ApiParam(name = "form", value = "导入账户表单数据", required = true)
                                        AccountImportForm form) {
        String keyStore = form.getAccountKeyStore();
        if (null == keyStore) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        String password = form.getPassword();
        if(null != password && !StringUtils.validPassword(password)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        AccountKeyStoreDto accountKeyStoreDto = null;
        try {
            accountKeyStoreDto = JSONUtils.json2pojo(keyStore, AccountKeyStoreDto.class);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.importAccountFormKeyStore(accountKeyStoreDto.toAccountKeyStore(), password).getData();
        return Result.getSuccess().setData(account.getAddress().toString());
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据私钥导入账户 [3.4.7]", notes = "返回账户地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Result.class)
    })
    public Result<String> importAccountByPriKey(@ApiParam(name = "form", value = "导入账户表单数据", required = true)
                                                    AccountImportPrikeyForm form) {
        String priKey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        String password = form.getPassword();
        if(null != password && !StringUtils.validPassword(password)){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Account account = accountService.importAccount(priKey, password).getData();
        return Result.getSuccess().setData(account.getAddress().toString());
    }

    @POST
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "移除账户", notes = "Nuls_RPC_API文档[3.4.9]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Result.class)
    })
    public Result removeAccount(@ApiParam(name = "钱包移除账户表单数据", value = "JSONFormat", required = true)
                                           AccountAPForm form) {
        if (!StringUtils.validPassword(form.getPassword()) || !Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return accountService.removeAccount(form.getAddress(), form.getPassword());
    }
}
