/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.*;
import io.nuls.account.rpc.model.*;
import io.nuls.account.rpc.model.form.*;
import io.nuls.account.service.AccountBaseService;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.account.util.AccountTool;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.crypto.AESEncrypt;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.protocol.model.validator.TxMaxSizeValidator;
import io.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
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

    @Autowired
    private ContractService contractService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    private Map<String, ScheduledFuture> accountUnlockSchedulerMap = new HashMap<>();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[创建] 创建账户 ", notes = "result.data: List<String>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
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
        Map<String, List<String>> map = new HashMap<>();
        map.put("list", list);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @POST
    @Path("/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[创建] 创建离线账户, 该账户不保存到数据库, 并将直接返回账户的所有信息 ", notes = "result.data: List<Account>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult createOfflineAccount(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                                        AccountCreateForm form) {
        int count = form.getCount() < 1 ? 1 : form.getCount();
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        List<AccountOfflineDto> accounts = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                if (StringUtils.isNotBlank(password)) {
                    account.encrypt(password);
                }
                accounts.add(new AccountOfflineDto(account));
            }
        } catch (NulsException e) {
            return Result.getFailed().toRpcClientResult();
        }
        Map<String, List<AccountOfflineDto>> map = new HashMap<>();
        map.put("list", accounts);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[查询] 查询账户列表", notes = "result.data: Page<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
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
        Collection<Account> accounts = accountService.getAccountList().getData();
        List<Account> accountList = new ArrayList<>(accounts);
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
    @ApiOperation("[查询] 查询账户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult get(@ApiParam(name = "address", value = "账户地址", required = true)
                               @PathParam("address") String address) {
        if (!AddressTool.validAddress(address)) {
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
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountService.isEncrypted(address);
        Map<String, Boolean> map = new HashMap<>();
        map.put("value", (Boolean) result.getData());
        result.setData(map);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/password/validation/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[验证密码] 验证账户密码是否正确")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult validationPassword(@PathParam("address") String address,
                                              @ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                                      AccountPasswordForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getPassword())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result<Account> rs = accountService.getAccount(address);
        if (rs.isFailed() || null == rs.getData()) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        Account account = rs.getData();
        boolean result = account.validatePassword(form.getPassword());
        Map<String, Boolean> map = new HashMap<>(2);
        map.put("value", result);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @POST
    @Path("/alias/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名] 设置别名")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult alias(@PathParam("address") String address,
                                 @ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                         AccountAliasForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = aliasService.setAlias(address, form.getAlias().trim(), form.getPassword());
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @GET
    @Path("/alias/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名手续费] 获取设置别名手续 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult aliasFee(@BeanParam() AccountAliasFeeForm form) {
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = aliasService.getAliasFee(form.getAddress(), form.getAlias());

        Long fee = null;
        Long maxAmount = null;
        Map<String, Long> map = new HashMap<>();
        if (result.isSuccess()) {
            fee = ((Na) result.getData()).getValue();
            //如果手续费大于理论最大值，则说明交易过大，需要计算最大交易金额
            long feeMax = TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES.multiply(TxMaxSizeValidator.MAX_TX_BYTES).getValue();
            if(fee > feeMax){
                AliasTransaction tx = new AliasTransaction();
                tx.setTime(TimeService.currentTimeMillis());
                Alias alias = new Alias(AddressTool.getAddress(form.getAddress()), form.getAlias());
                tx.setTxData(alias);
                try {
                    CoinDataResult coinDataResult = accountLedgerService.getCoinData(AddressTool.getAddress(form.getAddress()), AccountConstant.ALIAS_NA, tx.size(), TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
                    if (!coinDataResult.isEnough()) {
                        return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE).toRpcClientResult();
                    }
                    CoinData coinData = new CoinData();
                    coinData.setFrom(coinDataResult.getCoinList());
                    Coin change = coinDataResult.getChange();
                    if (null != change) {
                        //创建toList
                        List<Coin> toList = new ArrayList<>();
                        toList.add(change);
                        coinData.setTo(toList);
                    }
                    Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
                    coinData.addTo(coin);
                    tx.setCoinData(coinData);
                } catch (Exception e) {
                    Log.error(e);
                    return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
                }
                Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(form.getAddress()), tx,
                        TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
                if (rs.isSuccess()) {
                    maxAmount = ((Na) rs.getData()).getValue();
                }
            }
            map.put("fee", fee);
            map.put("maxAmount", maxAmount);
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @GET
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[验证别名是否可用] 验证别名是否可用(是否没有没占用) ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult isAliasUsable(@ApiParam(name = "alias", value = "别名", required = true) @QueryParam("alias") String alias) {
        if (StringUtils.isBlank(alias)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Map<String, Boolean> map = new HashMap<>();
        map.put("value", aliasService.isAliasUsable(alias));
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @GET
    @Path("/alias/address")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名获取地址] 根据别名获取账户地址地址 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getAddressByAlias(@ApiParam(name = "alias", value = "别名", required = true) @QueryParam("alias") String alias) {
        if (StringUtils.isBlank(alias)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Alias aliasObj = aliasService.getAlias(alias);
        if (null == aliasObj) {
            return new RpcClientResult(false, AccountErrorCode.ALIAS_NOT_EXIST);
        }
        Map<String, String> map = new HashMap<>();
        map.put("value", AddressTool.getStringAddressByBytes(aliasObj.getAddress()));
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[余额] 查询本地所有账户总余额")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
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
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getAssets(@ApiParam(name = "address", value = "账户地址", required = true)
                                     @PathParam("address") String address,
                                     @ApiParam(name = "pageNumber", value = "页码", required = true)
                                     @QueryParam("pageNumber") Integer pageNumber,
                                     @ApiParam(name = "pageSize", value = "每页条数", required = false)
                                     @QueryParam("pageSize") Integer pageSize) {
        try {
            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (!AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            Address addr = new Address(address);
            Result<Balance> balanceResult = accountLedgerService.getBalance(addr.getAddressBytes());
            if (balanceResult.isFailed()) {
                return balanceResult.toRpcClientResult();
            }
            Balance balance = balanceResult.getData();
            List<AssetDto> dtoList = new ArrayList<>();
            dtoList.add(new AssetDto("NULS", balance));

            Result<List<ContractTokenInfo>> allTokenListResult = contractService.getAllTokensByAccount(address);
            if (allTokenListResult.isSuccess()) {
                List<ContractTokenInfo> tokenInfoList = allTokenListResult.getData();
                if (tokenInfoList != null && tokenInfoList.size() > 0) {
                    for (ContractTokenInfo tokenInfo : tokenInfoList) {
                        if (tokenInfo.isLock()) {
                            continue;
                        }
                        dtoList.add(new AssetDto(tokenInfo));
                    }
                }
            }

            Result result = Result.getSuccess();
            List<AssetDto> infoDtoList = new ArrayList<>();
            Page<AssetDto> page = new Page<>(pageNumber, pageSize, dtoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                result.setData(page);
                return result.toRpcClientResult();
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            if (dtoList.size() > 0) {
                for (int i = start; i < end; i++) {
                    infoDtoList.add(dtoList.get(i));
                }
            }
            page.setList(infoDtoList);

            result.setSuccess(true);
            result.setData(page);

            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
            return result.toRpcClientResult();
        }

    }

    @POST
    @Path("/prikey/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[私钥查询] 查询账户私钥，只能查询本地创建或导入的账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getPrikey(@PathParam("address") String address, @ApiParam(name = "form", value = "密码表单数据", required = true)
            AccountPasswordForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountBaseService.getPrivateKey(address, form.getPassword());
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[获取本地所有账户的私钥] 调用获取本地账户私钥时，本地账户的加密信息必须一致(要么都没有密码，要么所有账户密码是一致的），否则将返回错误信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getAllPrikey(@ApiParam(name = "form", value = "密码表单数据") AccountPasswordForm form) {
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result result = accountBaseService.getAllPrivateKey(form.getPassword());
        return result.toRpcClientResult();
    }

    @GET
    @Path("/validate/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[验证地址格式是否正确]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult validateAddress(@ApiParam(name = "address", value = "账户地址", required = true)
                                           @PathParam("address") String address) {
        Result result = Result.getSuccess();
        Map<String, Object> map = new HashMap<>();
        map.put("value", AddressTool.validAddress(address));
        result.setData(map);
        return result.toRpcClientResult();

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
        Map<String, Boolean> map = new HashMap<>();
        map.put("value", true);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }


    @POST
    @Path("/unlock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[解锁] 解锁账户")
    public RpcClientResult unlock(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                                  @ApiParam(name = "form", value = "解锁表单数据", required = true)
                                          AccountUnlockForm form) {
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
        String password = form.getPassword();
        Integer unlockTime = form.getUnlockTime();
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
        Map<String, Boolean> map = new HashMap<>();
        map.put("value", true);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @POST
    @Path("/remark/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "设置账户备注", notes = "备注只在本地显示")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult setRemark(@ApiParam(name = "address", value = "账户地址", required = true)
                                     @PathParam("address") String address,
                                     @ApiParam(name = "form", value = "备注") AccountRemarkForm accountRemarkForm) {
        String remark = accountRemarkForm.getRemark();
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(remark)) {
            remark = null;
        }
        Result result = accountBaseService.setRemark(address, remark);
        if (result.isSuccess()) {
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", (Boolean) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[设密码] 设置账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult setPassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                       @PathParam("address") String address,
                                       @ApiParam(name = "form", value = "密码表单数据", required = true)
                                               AccountPasswordForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        if (StringUtils.isBlank(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        Result result = accountBaseService.setPassword(address, password);
        if (result.isSuccess()) {
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", (Boolean) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/offline/password/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[设密码] 设置离线账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult setPassword(@ApiParam(name = "form", value = "设置离线账户密码表单数据", required = true)
                                               OfflineAccountPasswordForm form) {
        String address = form.getAddress();
        String priKey = form.getPriKey();
        String password = form.getPassword();

        if (StringUtils.isBlank(address) || !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(priKey) || !ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(password) || !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }

        //验证地址是否正确
        ECKey key = ECKey.fromPrivate(new BigInteger(1, Hex.decode(priKey)));
        try {
            String newAddress = AccountTool.newAddress(key).getBase58();
            if (!newAddress.equals(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        try {
            Account account = AccountTool.createAccount(priKey);
            account.encrypt(password);
            Map<String, String> map = new HashMap<>();
            map.put("value", Hex.encode(account.getEncryptedPriKey()));
            return Result.getSuccess().setData(map).toRpcClientResult();
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED).toRpcClientResult();
        }
    }

    @PUT
    @Path("/offline/password/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[离线钱包修改密码] 根据原密码修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult updatePassword(@ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                                  OfflineAccountPasswordForm form) {
        String address = form.getAddress();
        String priKey = form.getPriKey();
        String password = form.getPassword();
        String newPassword = form.getNewPassword();

        if (StringUtils.isBlank(address) || !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(password) || !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        if (StringUtils.isBlank(newPassword) || !StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }

        try {
            byte[] priKeyBytes = AESEncrypt.decrypt(Hex.decode(priKey), password);
            Account tempAccount = AccountTool.createAccount(Hex.encode(priKeyBytes));
            if (!address.equals(tempAccount.getAddress().getBase58())) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            Result result = getEncryptedPrivateKey(address, Hex.encode(priKeyBytes), newPassword);
            if (result.isSuccess()) {
                Map<String, Boolean> map = new HashMap<>();
                map.put("value", (Boolean) result.getData());
                result.setData(map);
            }
            return result.toRpcClientResult();
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
    }

    public Result getEncryptedPrivateKey(String address, String priKey, String password) {
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account;
        try {
            account = AccountTool.createAccount(priKey);
            if (!address.equals(account.getAddress().getBase58())) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            account.encrypt(password);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        return Result.getSuccess().setData(Hex.encode(account.getEncryptedPriKey()));
    }

    @PUT
    @Path("/password/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据原密码修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult updatePassword(@ApiParam(name = "address", value = "账户地址", required = true)
                                          @PathParam("address") String address,
                                          @ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                                  AccountUpdatePasswordForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String password = form.getPassword();
        String newPassword = form.getNewPassword();
        if (StringUtils.isBlank(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        Result result = accountBaseService.changePassword(address, password, newPassword);
        if (result.isSuccess()) {
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", (Boolean) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }


    @PUT
    @Path("/password/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据私钥修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult updatePasswordByPriKey(@ApiParam(name = "form", value = "修改账户密码表单数据", required = true)
                                                          AccountPriKeyChangePasswordForm form) {

        String prikey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        String newPassword = form.getPassword();
        if (StringUtils.isBlank(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG).toRpcClientResult();
        }
        Result result = accountService.importAccount(prikey, newPassword);
        if (result.isSuccess()) {
            Account account = (Account) result.getData();
            Map<String, String> map = new HashMap<>();
            map.put("value", account.getAddress().toString());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/password/keystore")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[修改密码] 根据AccountKeyStore修改账户密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
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
        if (result.isSuccess()) {
            Account account = (Account) result.getData();
            Map<String, String> map = new HashMap<>();
            map.put("value", account.getAddress().toString());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/export/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导出] 账户备份，导出AccountKeyStore字符串 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult export(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                                  @ApiParam(name = "form", value = "钱包备份表单数据")
                                          AccountKeyStoreBackup form, @Context HttpServletResponse response) {
        if (StringUtils.isNotBlank(address) && !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result<AccountKeyStore> result = accountService.exportAccountToKeyStore(address, form.getPassword());
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        AccountKeyStore accountKeyStore = result.getData();
        String filePath = form.getPath();
        if(StringUtils.isBlank(filePath)) {
            URL resource = ClassLoader.getSystemClassLoader().getResource("");
            try {
                filePath = URLDecoder.decode(resource.getPath(), "UTF-8") + AccountConstant.ACCOUNTKEYSTORE_FOLDER_NAME;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                filePath = resource.getPath() + AccountConstant.ACCOUNTKEYSTORE_FOLDER_NAME;
            }
        }
        return backUpFile(filePath, new AccountKeyStoreDto(accountKeyStore)).toRpcClientResult();
    }



    /**
     * 输出文件流
     * Export file
     */
    private void backUpFile(AccountKeyStoreDto accountKeyStoreDto, HttpServletResponse response) {
        try {
            String fileName = accountKeyStoreDto.getAddress().concat(AccountConstant.ACCOUNTKEYSTORE_FILE_SUFFIX);
            //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("application/octet-stream");
            //2.设置文件头：最后一个参数是设置下载文件名
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            response.getOutputStream().write(JSONUtils.obj2json(accountKeyStoreDto).getBytes());
            response.getOutputStream().flush();
        } catch (Exception e) {
            Log.error("Export Exception!");
        }
    }


    /**
     * 直接生成文件
     * Export file
     */
    private Result backUpFile(String path, AccountKeyStoreDto accountKeyStoreDto) {
        File backupFile = new File(path);
        //if not directory , create directory
        if (!backupFile.isDirectory()) {
            if (!backupFile.mkdirs()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
            if (!backupFile.exists() && !backupFile.mkdir()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
        }
        String fileName = accountKeyStoreDto.getAddress().concat(AccountConstant.ACCOUNTKEYSTORE_FILE_SUFFIX);
        backupFile = new File(backupFile, fileName);
        try {
            if (!backupFile.exists() && !backupFile.createNewFile()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
        } catch (IOException e) {
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(backupFile);
            fileOutputStream.write(JSONUtils.obj2json(accountKeyStoreDto).getBytes());
        } catch (Exception e) {
            return Result.getFailed(KernelErrorCode.PARSE_JSON_FAILD);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        //If it is a windows system path, remove the first /
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1 && path.startsWith("/")) {
            path = path.substring(1);
            path = path.replace("/", "\\");
        }

        Map<String, String> map = new HashMap<>();
        map.put("value", path + File.separator + fileName);
        return Result.getSuccess().setData(map);
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 根据AccountKeyStore导入账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
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
        if (result.isSuccess()) {
            Account account = (Account) result.getData();
            Map<String, String> map = new HashMap<>();
            map.put("value", account.getAddress().toString());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/import/keystore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "[导入] 根据AccountKeyStore导入账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult importAccountByKeystoreFile(@FormDataParam("keystore") InputStream in,
                                                       @FormDataParam("password") String password,
                                                       @FormDataParam("overwrite") Boolean overwrite) {

        if (null == in || null == overwrite) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result<AccountKeyStoreDto> rs = getAccountKeyStoreDto(in);
        if (rs.isFailed()) {
            return rs.toRpcClientResult();
        }
        AccountKeyStoreDto accountKeyStoreDto = rs.getData();
        if (!overwrite) {
            Account account = accountService.getAccount(accountKeyStoreDto.getAddress()).getData();
            if (null != account) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST).toRpcClientResult();
            }
        }

        Result result = accountService.importAccountFormKeyStore(accountKeyStoreDto.toAccountKeyStore(), password);
        if (result.isSuccess()) {
            Account account = (Account) result.getData();
            Map<String, String> map = new HashMap<>();
            map.put("value", account.getAddress().toString());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }


    private Result<AccountKeyStoreDto> getAccountKeyStoreDto(InputStream in) {
        StringBuilder ks = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String str;
        try {
            inputStreamReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((str = bufferedReader.readLine()) != null) {
                if (!str.isEmpty()) {
                    ks.append(str);
                }
            }
            AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
            return Result.getSuccess().setData(accountKeyStoreDto);
        } catch (FileNotFoundException e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();

                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
    }


    @POST
    @Path("/import/prikeys")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 根据私钥集合导入账户,如果设置密码,则所有导入的账户为统一的密码, 所有账户不管本地是否已存在, 都会强制覆盖导入", notes = "返回账户地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult importAccountByPriKeys(@ApiParam(name = "form", value = "导入账户表单数据", required = true) AccountPriKeysPasswordForm form) {

        List<String> list = form.getPriKey();
        List<String> success = new ArrayList<>();
        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        for (String priKey : list) {
            if (!ECKey.isValidPrivteHex(priKey)) {
                Result result = Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
                result.setMsg(result.getMsg() + ", 已导入成功" + success.size() + "个");
                return result.toRpcClientResult();
            }
            Result result = accountService.importAccount(priKey, password);
            if (result.isSuccess()) {
                Account account = (Account) result.getData();
                success.add(account.getAddress().toString());
            } else {
                result.setMsg(result.getMsg() + ", 已导入成功" + success.size() + "个");
                return result.toRpcClientResult();
            }
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("list", success);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 根据私钥导入账户", notes = "返回账户地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult importAccountByPriKey(@ApiParam(name = "form", value = "导入账户表单数据", required = true) AccountPriKeyPasswordForm form) {

        if (null == form || null == form.getOverwrite()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        String priKey = form.getPriKey();
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!form.getOverwrite()) {
            ECKey key = null;
            try {
                key = ECKey.fromPrivate(new BigInteger(1, Hex.decode(form.getPriKey())));
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            Address address = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
            Account account = accountService.getAccount(address).getData();
            if (null != account) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_EXIST).toRpcClientResult();
            }
        }

        String password = form.getPassword();
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
        }
        Result result = accountService.importAccount(priKey, password);
        if (result.isSuccess()) {
            Account account = (Account) result.getData();
            Map<String, String> map = new HashMap<>();
            map.put("value", account.getAddress().toString());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/remove/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[移除] 移除账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult removeAccount(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address,
                                         @ApiParam(name = "钱包移除账户表单数据", value = "JSONFormat", required = true)
                                                 AccountPasswordForm form) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountService.removeAccount(address, form.getPassword());
        if (result.isSuccess()) {
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", (Boolean) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/createMultiAccount")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[创建] 创建多重签名账户 ", notes = "result.data: List<String>")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult createMultiAccount(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                                      MultiAccountCreateForm form) {
        if (null == form || null == form.getPubkeys() || form.getPubkeys().size() == 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (form.getM() == 0) {
            form.setM(form.getPubkeys().size());
        }
        if(form.getPubkeys().size() < form.getM()){
            return Result.getFailed(AccountErrorCode.SIGN_COUNT_TOO_LARGE).toRpcClientResult();
        }
        Set<String> pubkeySet = new HashSet<>(form.getPubkeys());
        if(pubkeySet.size() < form.getPubkeys().size()){
            return Result.getFailed(AccountErrorCode.PUBKEY_REPEAT).toRpcClientResult();
        }
        Result result = accountService.createMultiAccount(form.getPubkeys(), form.getM());
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        MultiSigAccountDto account = new MultiSigAccountDto((MultiSigAccount) result.getData());
        return Result.getSuccess().setData(account).toRpcClientResult();
    }


    @POST
    @Path("multiAccount/mutilAlias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名] 多签账户设置别名")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult alias(@ApiParam(name = "form", value = "多签账户设置别名表单数据", required = true)
                                              CreateMultiAliasForm form) {
        if(NulsContext.MAIN_NET_VERSION  <=1){
            return Result.getFailed(KernelErrorCode.VERSION_TOO_LOW).toRpcClientResult();
        }
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getSignAddress()) || !AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!StringUtils.validAlias(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG).toRpcClientResult();
        }
        if (!StringUtils.validAlias(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG).toRpcClientResult();
        }
        if (!aliasService.isAliasUsable(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.ALIAS_EXIST).toRpcClientResult();
        }
        Result result = aliasService.setMutilAlias(form.getAddress(),form.getSignAddress(),form.getAlias(),form.getPassword());
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("txData", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

	
    @POST
    @Path("/importMultiAccount")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导入] 导入多重签名账户 ", notes = "result.data: boolean ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult importMultiAccount(@ApiParam(name = "form", value = "多签账户表单数据", required = true)
                                                      MultiAccountImportForm form) {
        if (null == form || null == form.getPubkeys() || StringUtils.isBlank(form.getAddress()) || form.getPubkeys().size() == 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (form.getM() == 0) {
            form.setM(form.getPubkeys().size());
        }
        if(form.getPubkeys().size() < form.getM()){
            return Result.getFailed(AccountErrorCode.SIGN_COUNT_TOO_LARGE).toRpcClientResult();
        }
        Result result = accountService.saveMultiSigAccount(form.getAddress(), form.getPubkeys(), form.getM());
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        return result.toRpcClientResult();
    }

    @GET
    @Path("/multiAccount/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[导出] 导出账户信息，导出NultiSigAccount数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getMultiSigAccount(@ApiParam(name = "address", value = "账户地址", required = true)
                                              @PathParam("address") String address) throws Exception {
        if (StringUtils.isNotBlank(address) && !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result<MultiSigAccount> result = accountService.getMultiSigAccount(address);
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        return Result.getSuccess().setData(new MultiSigAccountDto(result.getData())).toRpcClientResult();
    }

    @DELETE
    @Path("/multiAccount/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[删除] 移除多签账户")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult delMultiSigAccount(@ApiParam(name = "address", value = "账户地址", required = true)
                                              @PathParam("address") String address) throws Exception {
        if (StringUtils.isNotBlank(address) && !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountService.removeMultiSigAccount(address);
        Map<String, Boolean> map = new HashMap<>();
        map.put("result", result.isSuccess());
        return result.setData(map).toRpcClientResult();
    }

    @GET
    @Path("/multiAccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[查询] 查询全部多签账户信息，返回NultiSigAccount列表数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult getMultiSigAccountList() throws Exception {

        Result<List<MultiSigAccount>> result = accountService.getMultiSigAccountList();
        if (result.isFailed()) {
            return result.toRpcClientResult();
        }
        List<MultiSigAccountDto> list = new ArrayList<>();
        for (MultiSigAccount account : result.getData()) {
            MultiSigAccountDto dto = new MultiSigAccountDto(account);
            list.add(dto);
        }
        return Result.getSuccess().setData(list).toRpcClientResult();
    }

    @GET
    @Path("multiAccount/alias/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[别名手续费] 获取设置别名手续 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult multiAliasFee(@BeanParam() MultiAliasFeeForm form) throws Exception{
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getAlias())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = aliasService.getMultiAliasFee(form.getAddress(), form.getAlias());
        AliasTransaction tx = new AliasTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        Result<MultiSigAccount> sigAccountResult = accountService.getMultiSigAccount(form.getAddress());
        MultiSigAccount multiSigAccount = sigAccountResult.getData();
        Script redeemScript = accountLedgerService.getRedeemScript(multiSigAccount);
        if(redeemScript == null){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        Alias alias = new Alias(AddressTool.getAddress(form.getAddress()), form.getAlias());
        tx.setTxData(alias);
        try {
            CoinDataResult coinDataResult = accountLedgerService.getMutilCoinData(AddressTool.getAddress(form.getAddress()), AccountConstant.ALIAS_NA, tx.size(), TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE).toRpcClientResult();
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }
            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
            coinData.addTo(coin);
            tx.setCoinData(coinData);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
        //交易签名的长度为m*单个签名长度+赎回脚本长度
        int scriptSignLenth = redeemScript.getProgram().length + ((int)multiSigAccount.getM()) * 72;
        Result rs = accountLedgerService.getMultiMaxAmountOfOnce(AddressTool.getAddress(form.getAddress()), tx, TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES,scriptSignLenth);
        Map<String, Long> map = new HashMap<>();
        Long fee = null;
        Long maxAmount = null;
        if (result.isSuccess()) {
            fee = ((Na) result.getData()).getValue();
        }
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", fee);
        map.put("maxAmount", maxAmount);
        result.setData(map);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/signMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[消息签名] 通过私钥对消息进行签名, 返回签名结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult signMessage(@ApiParam(name = "form", value = "消息签名表单数据", required = true)
                                                   SignMessageForm form) {
        if (form == null) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        String signatureBase64 = null;
        String address = form.getAddress();
        String password = form.getPassword();
        String message = form.getMessage();
        //check the parameter
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(message)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        try {
            Result rs = accountService.signMessage(address,password,message);
            if (rs.isSuccess()) {
                signatureBase64 = (String) rs.getData();
            }
            Map<String, String> map = new HashMap<>();
            map.put("signatureBase64", signatureBase64);
            return Result.getSuccess().setData(map).toRpcClientResult();
        } catch (Exception e) {
            Log.error("",e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

    @POST
    @Path("/verifyMessageSignature")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "[验证消息签名] 对消息的签名账户进行验证", notes = "result.data: List<Account>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult verifySignMessage(@ApiParam(name = "form", value = "验证消息签名数据", required = true)
                                                        VerifyMessageSignatureForm form) {
        //check the parameter
        if (form == null) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Boolean result = false;
        String address = form.getAddress();
        String signatureBase64 = form.getSignatureBase64();
        String message = form.getMessage();
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(message)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(signatureBase64)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        try {
            Result rs = accountService.verifyMessageSignature(address,message,signatureBase64);
            if (rs.isSuccess()) {
                result = (Boolean) rs.getData();
            }
            Map<String, Boolean> map = new HashMap<>();
            map.put("result", result);
            return Result.getSuccess().setData(map).toRpcClientResult();
        } catch (Exception e) {
            Log.error("",e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

}
