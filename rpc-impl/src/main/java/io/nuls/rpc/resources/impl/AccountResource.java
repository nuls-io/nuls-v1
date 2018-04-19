/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.model.Result;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Na;
import io.nuls.rpc.entity.*;
import io.nuls.rpc.resources.form.AccountAliasForm;
import io.nuls.rpc.resources.form.AccountCreateForm;
import io.nuls.rpc.resources.form.AccountAPForm;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/account")
@Api(value = "/browse", description = "Account")
public class AccountResource {

    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    public AccountResource() {
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建账户 [3.3.1]", notes = "result.data: Page<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = ArrayList.class)
    })
    public RpcResult create(@ApiParam(name = "form", value = "账户表单数据", required = true)
                                        AccountCreateForm form) {
        Result<List<String>> accountResult = accountService.createAccount(form.getCount(), form.getPassword());
        RpcResult result = new RpcResult(accountResult);
        if (result.isSuccess()) {
            NulsContext.setCachedPasswordOfWallet(form.getPassword());
        }
        return result;
    }

    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户信息 [3.3.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = AccountDto.class)
    })
    public RpcResult get(@ApiParam(name = "address", value = "账户地址 ，缺省时默认为所有账户",required = true)
                             @PathParam("address") String address) {
        RpcResult result;
        if (!Address.validAddress(address)) {
            result = RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
            return result;
        }
        Account account = accountService.getAccount(address);
        if (account == null) {
            Alias alias = accountService.getAlias(address);
            if (alias == null) {
                result = RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
            } else {
                AccountDto dto = new AccountDto();
                dto.setAddress(address);
                dto.setAlias(alias.getAlias());
                result = RpcResult.getSuccess().setData(dto);

            }
        } else {
            result = RpcResult.getSuccess().setData(new AccountDto(account));
        }
        return result;
    }

    @POST
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("设置别名 [3.3.6]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = RpcResult.class)
    })
    public RpcResult alias(@ApiParam(name = "form", value = "设置别名表单数据", required = true)
                                       AccountAliasForm form) {
        if (!Address.validAddress(form.getAddress())) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }
        Result result = accountService.setAlias(form.getAddress(), form.getPassword(), form.getAlias());
        RpcResult rpcResult = new RpcResult(result);
        return rpcResult;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户列表 [3.3.4]", notes = "result.data: Page<AccountDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = AccountDto.class)
    })
    public RpcResult accountList(@ApiParam(name="pageNumber",value="页码")
                                     @QueryParam("pageNumber") int pageNumber,
                                 @ApiParam(name="pageSize",value="每页条数")
                                 @QueryParam("pageSize") int pageSize) {
        if (pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        Page<Account> page = accountService.getAccountList(pageNumber, pageSize);
        List<AccountDto> dtoList = new ArrayList<>();
        for (Account account : page.getList()) {
            dtoList.add(new AccountDto(account));
        }

        Page<AccountDto> dtoPage = new Page<>(page);
        dtoPage.setList(dtoList);
        RpcResult result = RpcResult.getSuccess();
        result.setData(dtoPage);
        return result;
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户余额 [3.3.3]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BalanceDto.class)
    })
    public RpcResult getBalance(@ApiParam(name="address", value="账户地址", required = true)
                                    @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }
        Balance balance = ledgerService.getBalance(address);
        RpcResult result = RpcResult.getSuccess();
        result.setData(new BalanceDto(balance));
        return result;
    }

    @GET
    @Path("/balances")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询当前钱包所有账户余额(合计) [3.3.9]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BalanceDto.class)
    })
    public RpcResult getBalance() {
        Balance balance = ledgerService.getBalance(null);
        RpcResult result = RpcResult.getSuccess();
        result.setData(new BalanceDto(balance));
        return result;
    }

    @GET
    @Path("/utxo/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户足够数量的未花费输出 [3.3.5]", notes = "result.data: List<OutputDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = OutputDto.class)
    })
    public RpcResult getUtxo(@ApiParam(name="address", value="账户地址", required = true)
                                 @QueryParam("address") String address,
                             @ApiParam(name="amount", value="Nuls数量", required = true)
                             @QueryParam("amount") long amount) {
        if (!Address.validAddress(address) || amount <= 0 || amount > Na.MAX_NA_VALUE) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        UtxoBalance balance = (UtxoBalance) ledgerService.getBalance(address);
        if (balance == null || balance.getUnSpends() == null) {
            return RpcResult.getFailed("balance not enough");
        }
        amount += this.ledgerService.getTxFee(Integer.MAX_VALUE).getValue();

        long usable = 0;
        boolean enough = false;
        List<OutputDto> dtoList = new ArrayList<>();
        for (int i = 0; i < balance.getUnSpends().size(); i++) {
            UtxoOutput output = balance.getUnSpends().get(i);
            if (output.isUsable()) {
                usable += output.getValue();
                dtoList.add(new OutputDto(output));
            }
            if (usable > amount) {
                enough = true;
                break;
            }
        }

        if (!enough) {
            return RpcResult.getFailed("balance not enough");
        }

        return RpcResult.getSuccess().setData(dtoList);
    }

    @POST
    @Path("/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询账户私钥，只能查询本地创建或导入的账户 [3.3.7]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = String.class)
    })
    public RpcResult getPrikey(@ApiParam(name = "form", value = "查询私钥表单数据", required = true)
                                       AccountAPForm form) {
        if (!Address.validAddress(form.getAddress()) || !StringUtils.validPassword(form.getPassword())) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }

        Result result = accountService.getPrivateKey(form.getAddress(), form.getPassword());
        return new RpcResult(result);
    }

    @GET
    @Path("/assets/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询账户资产 [3.3.8]", notes = "result.data: List<AssetDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = AssetDto.class)
    })
    public RpcResult getAssets(@ApiParam(name="address", value="账户地址", required = true)
                                   @PathParam("address") String address) {
        if (!Address.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }

        Balance balance = ledgerService.getBalance(address);
        RpcResult result = RpcResult.getSuccess();
        List<AssetDto> dtoList = new ArrayList<>();
        dtoList.add(new AssetDto("NULS", balance));
        result.setData(dtoList);
        return result;
    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAddress(@QueryParam("publicKey") String publicKey,
                                @QueryParam("subChainId") Integer subChainId) {
        if (subChainId < 1 || subChainId >= 65535) {
            return RpcResult.getFailed(ErrorCode.CHAIN_ID_ERROR);
        }
        Address address = new Address((short) subChainId.intValue(), Hex.decode(publicKey));
        RpcResult result = RpcResult.getSuccess();
        result.setData(address.toString());
        return result;
    }

    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult lock(@QueryParam("address") String address, @QueryParam("password") String password,
                          @QueryParam("amount") long amount, @QueryParam("remark") String remark,
                          @QueryParam("unlockTime") Long unlockTime) {
        address = StringUtils.formatStringPara(address);
        if (!Address.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }

        password = StringUtils.formatStringPara(password);

        Result lockResult = ledgerService.lock(address, password, Na.parseNuls(amount), unlockTime, remark);
        RpcResult result;
        if (lockResult.isSuccess()) {
            result = RpcResult.getSuccess();
        } else {
            result = RpcResult.getFailed(lockResult.getMessage());
        }
        result.setData(lockResult.getObject());
        return result;
    }

}
