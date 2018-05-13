package io.nuls.account.rpc.resources;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.rpc.model.AccountDto;
import io.nuls.account.rpc.model.form.AccountAliasForm;
import io.nuls.account.rpc.model.form.AccountCreateForm;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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
}
