package io.nuls.account.rpc.resources;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.rpc.model.AccountDto;
import io.nuls.account.rpc.model.AssetDto;
import io.nuls.account.rpc.model.BalanceDto;
import io.nuls.account.rpc.model.form.AccountAPForm;
import io.nuls.account.rpc.model.form.AccountAliasForm;
import io.nuls.account.rpc.model.form.AccountCreateForm;
import io.nuls.account.rpc.model.form.AccountPasswordForm;
import io.nuls.account.service.AccountBaseService;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Na;
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
    private AccountCacheService accountCacheService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建账户 [3.3.1]", notes = "result.data: List<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ArrayList.class)
    })
    public Result<List<AccountDto>> create(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                 AccountCreateForm form) {
        int count = form.getCount() < 1 ? 1 : form.getCount();
        String password = form.getPassword();
        if(null == form.getPassword() || "".equals(form.getPassword())){
            password = null;
        }
        List<Account> listAccount = accountService.createAccount(count, password).getData();
        List<AccountDto> listAccountDto = new ArrayList<>();
        for (Account account : listAccount) {
            listAccountDto.add(new AccountDto(account));
        }
        return Result.getSuccess().setData(listAccountDto);
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
        if(null == account){
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
        if(StringUtils.isBlank(form.getAlias()) || !StringUtils.validPassword(form.getPassword())){
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
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
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
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
        Balance balance = accountService.getBalance(address).getData();
        if(balance == null) {
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
        Balance balance = accountService.getBalance().getData();
        Result result = Result.getSuccess();
        result.setData(new BalanceDto(balance));
        return result;
    }

    @GET
    @Path("/utxo/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户足够数量的未花费输出 [3.3.5]", notes = "result.data: List<OutputDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"/*, response = OutputDto.class*/)
    })
    public Result getUtxo(@ApiParam(name = "address", value = "账户地址", required = true)
                             @QueryParam("address") String address,
                             @ApiParam(name = "amount", value = "Nuls数量", required = true)
                             @QueryParam("amount") long amount) {
        if (!Address.validAddress(address) || amount <= 0 || amount > Na.MAX_NA_VALUE) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
        // todo
       /* amount += this.ledgerService.getTxFee(Integer.MAX_VALUE).getValue();

        UtxoBalance balance = (UtxoBalance) ledgerService.getAccountUtxo(address, Na.valueOf(amount));
        if (balance == null || balance.getUnSpends() == null || balance.getUnSpends().isEmpty()) {
            return Result.getFailed("balance not enough");
        }

        List<OutputDto> dtoList = new ArrayList<>();
        for (int i = 0; i < balance.getUnSpends().size(); i++) {
            dtoList.add(new OutputDto(balance.getUnSpends().get(i)));
        }

        return Result.getSuccess().setData(dtoList);
        */
       return null;
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

        Balance balance = accountService.getBalance(address).getData();
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

    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "锁定账户地址", notes = "")
    public Result lock(@QueryParam("address") String address, @QueryParam("password") String password) {
        Account account = accountService.getAccount(address).getData();
        if(null == account){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            account.encrypt(password);
            accountCacheService.putAccount(account);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }

        return Result.getSuccess();
    }

    private static final int MAX_UNLOCK_TIME = 60;
    @POST
    @Path("/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "解锁账户", notes = "")
    public Result unlock(@QueryParam("address") String address, @QueryParam("password") String password) {
        Account account = accountService.getAccount(address).getData();
        if(null == account){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            account.decrypt(password);
            accountCacheService.putAccount(account);
            // 解锁120秒后自动锁定
            ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.schedule(() -> {
                try {
                    account.encrypt(password);
                    accountCacheService.putAccount(account);
                } catch (NulsException e) {
                    Log.error("account.encrypt Exception : account:" + account.getAddress().toString());
                } finally {
                    accountCacheService.removeAccount(account.getAddress());
                    scheduler.shutdown();
                }
            },MAX_UNLOCK_TIME, TimeUnit.SECONDS);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return Result.getSuccess();
    }


    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "重置钱包密码")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Result.class)
    })
    public Result password(@ApiParam(name = "form", value = "重置钱包密码表单数据", required = true)
                                   AccountPasswordForm form) {
        Result result = this.accountBaseService.changePassword(form.getAddress(), form.getPassword(), form.getNewPassword());
        if (result.isSuccess()) {
            //NulsContext.setCachedPasswordOfWallet(form.getNewPassword());
        }
        return result;
    }

}
