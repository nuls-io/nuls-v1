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
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.SerializeUtils;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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

    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    private Map<String, ScheduledFuture> accountUnlockSchedulerMap = new HashMap<>();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[创建] 创建账户 ", notes = "result.data: List<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ArrayList.class)
    })
    public RpcClientResult create(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                          AccountCreateForm form) {
        int count = form.getCount() < 1 ? 1 : form.getCount();
        String password = form.getPassword();
        if (StringUtils.isBlank(password)) {
            password = null;
        }
        Result result = accountService.createAccount(count, password);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        List<Account> listAccount = (List<Account>) result.getData();
        List<String> list = new ArrayList<>();
        for (Account account : listAccount) {
            list.add(account.getAddress().toString());
        }
        return Result.getSuccess().setData(list).toRpcClientResult();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[查询] 查询账户列表 [3.3.4]", notes = "result.data: Page<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountDto.class)
    })
    public RpcClientResult accountList(@ApiParam(name = "pageNumber", value = "页码")
                                       @QueryParam("pageNumber") int pageNumber,
                                       @ApiParam(name = "pageSize", value = "每页条数")
                                       @QueryParam("pageSize") int pageSize) {
        if (pageNumber < 0 || pageSize < 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
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
            return Result.getSuccess().setData(page).toRpcClientResult();
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
        return Result.getSuccess().setData(resultPage).toRpcClientResult();
    }

    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[查询] 查询账户信息 [3.3.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountDto.class)
    })
    public RpcClientResult get(@ApiParam(name = "address", value = "账户地址", required = true)
                               @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        return Result.getSuccess().setData(new AccountDto(account)).toRpcClientResult();
    }

    @GET
    @Path("/encrypted/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[是否加密] 根据账户地址获取账户是否加密")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult isEncrypted(@ApiParam(name = "address", value = "账户地址", required = true)
                               @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        return accountService.isEncrypted(address).setData(address).toRpcClientResult();
    }

    @POST
    @Path("/alias/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名] 设置别名 [3.3.6]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult alias(@PathParam("address") String address,
                                 @ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                         AccountAliasForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        return aliasService.setAlias(address, form.getAlias().trim(), form.getPassword()).toRpcClientResult();
    }

    @GET
    @Path("/alias/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名手续费] 获取设置别名手续 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult aliasFee(@BeanParam() AccountAliasFeeForm form) {
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        return accountService.getAliasFee(form.getAddress(), form.getAlias()).toRpcClientResult();
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[余额] 查询账户余额 [3.3.3]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BalanceDto.class)
    })
    public RpcClientResult getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                      @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        try {
            Address addr = new Address(address);
            Result<Balance> result = accountLedgerService.getBalance(addr.getBase58Bytes());
            if (result.isFailed()) {
                return result.toRpcClientResult();
            }
            Balance balance = result.getData();
            return Result.getSuccess().setData(new BalanceDto(balance)).toRpcClientResult();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED).toRpcClientResult();
        }
    }

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[余额] 查询本地所有账户总余额")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BalanceDto.class)
    })
    public RpcClientResult getTotalBalance() {
        try {
            return accountService.getBalance().toRpcClientResult();
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED).toRpcClientResult();
        }
    }

    @GET
    @Path("/assets/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[资产] 查询账户资产 [3.3.8]", notes = "result.data: List<AssetDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AssetDto.class)
    })
    public RpcClientResult getAssets(@ApiParam(name = "address", value = "账户地址", required = true)
                                     @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        try {
            Address addr = new Address(address);
            Result<Balance> result = accountLedgerService.getBalance(addr.getBase58Bytes());
            if (result.isFailed()) {
                return result.toRpcClientResult();
            }
            Balance balance = result.getData();
            List<AssetDto> dtoList = new ArrayList<>();
            dtoList.add(new AssetDto("NULS", balance));
            return Result.getSuccess().setData(dtoList).toRpcClientResult();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED).toRpcClientResult();
        }
    }

    @POST
    @Path("/prikey/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[私钥查询] 查询账户私钥，只能查询本地创建或导入的账户 [3.3.7]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getPrikey(@PathParam("address") String address, @ApiParam(name = "form", value = "查询私钥表单数据", required = true)
            AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        return accountBaseService.getPrivateKey(address, form.getPassword()).toRpcClientResult();
    }

    @POST
    @Path("/lock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[锁账户] 清除缓存的锁定账户", notes = "Clear the cache unlock account.")
    public RpcClientResult lock(@ApiParam(name = "address", value = "账户地址", required = true) @PathParam("address") String address) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        accountCacheService.removeAccount(account.getAddress());
        BlockingQueue<Runnable> queue = scheduler.getQueue();
        String addr = account.getAddress().toString();
        Runnable scheduledFuture = (Runnable) accountUnlockSchedulerMap.get(addr);
        if (queue.contains(scheduledFuture)) {
            scheduler.remove(scheduledFuture);
            accountUnlockSchedulerMap.remove(addr);
        }
        return Result.getSuccess().toRpcClientResult();
    }


    @POST
    @Path("/unlock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[解锁] 解锁账户", notes = "")
    public RpcClientResult unlock(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                                  @ApiParam(name = "password", value = "账户密码", required = true)
                                  @QueryParam("password") String password,
                                  @ApiParam(name = "unlockTime", value = "解锁时间默认120秒(单位:秒)")
                                  @QueryParam("unlockTime") Integer unlockTime) {
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        String addr = account.getAddress().toString();
        //如果存在定时加锁任务, 先删除之前的任务
        if (accountUnlockSchedulerMap.containsKey(addr)) {
            BlockingQueue<Runnable> queue = scheduler.getQueue();
            Runnable sf = (Runnable) accountUnlockSchedulerMap.get(addr);
            if (queue.contains(sf)) {
                scheduler.remove(sf);
                accountUnlockSchedulerMap.remove(addr);
            }
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
            ScheduledFuture scheduledFuture = scheduler.schedule(() -> {
                accountCacheService.removeAccount(account.getAddress());
            }, unlockTime, TimeUnit.SECONDS);
            accountUnlockSchedulerMap.put(addr, scheduledFuture);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        return Result.getSuccess().toRpcClientResult();
    }


    @POST
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[设密码] 设置账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult setPassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                       @PathParam("address") String address,
                                       @ApiParam(name = "form", value = "设置钱包密码表单数据", required = true)
                                               AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        if (StringUtils.isBlank(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The password is required").toRpcClientResult();
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        return accountBaseService.setPassword(address, password).toRpcClientResult();
    }

    @PUT
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据原密码修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult updatePassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                          @PathParam("address") String address,
                                          @ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                                  AccountUpdatePasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        String newPassword = form.getNewPassword();
        if (StringUtils.isBlank(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The password is required").toRpcClientResult();
        }
        if (StringUtils.isBlank(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The newPassword is required").toRpcClientResult();
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        return this.accountBaseService.changePassword(address, password, newPassword).toRpcClientResult();
    }

    @PUT
    @Path("/password/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据私钥修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult updatePasswordByPriKey(@ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                                          AccountPriKeyChangePasswordForm form) {

        String prikey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The prikey is wrong").toRpcClientResult();
        }
        String newPassword = form.getPassword();
        if (StringUtils.isBlank(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The newPassword is required").toRpcClientResult();
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        Result result = accountService.importAccount(prikey, newPassword);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString()).toRpcClientResult();
    }

    @POST
    @Path("/password/keystore")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据AccountKeyStore修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult updatePasswordByAccountKeyStore(@ApiParam(name = "form", value = "重置密码表单数据", required = true)
                                                                   AccountKeyStoreResetPasswordForm form) {

        if (null == form || null == form.getAccountKeyStoreDto()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        AccountKeyStoreDto accountKeyStoreDto = form.getAccountKeyStoreDto();

        String password = form.getPassword();
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result result = accountService.updatePasswordByAccountKeyStore(accountKeyStoreDto.toAccountKeyStore(), password);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString()).toRpcClientResult();
    }


    @POST
    @Path("/export/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导出] 账户备份，导出AccountKeyStore字符串 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult export(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                                  @ApiParam(name = "form", value = "钱包备份表单数据")
                                          AccountPasswordForm form) {
        if (StringUtils.isNotBlank(address) && !Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result<AccountKeyStore> result = accountService.exportAccountToKeyStore(address, form.getPassword());
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        AccountKeyStore accountKeyStore = result.getData();
        return Result.getSuccess().setData(new AccountKeyStoreDto(accountKeyStore)).toRpcClientResult();
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 根据AccountKeyStore导入账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult importAccount(@ApiParam(name = "form", value = "导入账户表单数据", required = true)
                                                 AccountKeyStoreImportForm form) {

        if (null == form || null == form.getAccountKeyStoreDto() || null == form.getOverwrite()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        AccountKeyStoreDto accountKeyStoreDto = form.getAccountKeyStoreDto();
        if (!form.getOverwrite()) {
            Account account = accountService.getAccount(accountKeyStoreDto.getAddress()).getData();
            if (null != account) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST).toRpcClientResult();
            }
        }
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result result = accountService.importAccountFormKeyStore(accountKeyStoreDto.toAccountKeyStore(), password);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString()).toRpcClientResult();
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 根据私钥导入账户", notes = "返回账户地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult importAccountByPriKey(@ApiParam(name = "form", value = "导入账户表单数据", required = true) AccountPriKeyPasswordForm form) {

        if (null == form || null == form.getOverwrite()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!form.getOverwrite()) {
            ECKey key = null;
            try {
                key = ECKey.fromPrivate(new BigInteger(Hex.decode(form.getPriKey())));
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            Address address = new Address(NulsContext.DEFAULT_CHAIN_ID, SerializeUtils.sha256hash160(key.getPubKey()));
            Account account = accountService.getAccount(address).getData();
            if (null != account) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST).toRpcClientResult();
            }
        }
        String priKey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result result = accountService.importAccount(priKey, password);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        Account account = (Account) result.getData();
        return Result.getSuccess().setData(account.getAddress().toString()).toRpcClientResult();
    }

    @POST
    @Path("/remove/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[移除] 移除账户", notes = "Nuls_RPC_API文档[3.4.9]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Result.class)
    })
    public RpcClientResult removeAccount(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address,
                                         @ApiParam(name = "钱包移除账户表单数据", value = "JSONFormat", required = true)
                                                 AccountPasswordForm form) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        return accountService.removeAccount(address, form.getPassword()).toRpcClientResult();
    }
}
