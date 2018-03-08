/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.*;
import io.nuls.rpc.resources.form.AccountParamForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/account")
public class AccountResource {

    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult create(AccountParamForm form) {
        AssertUtil.canNotEmpty(form.getCount());
        AssertUtil.canNotEmpty(form.getPassword());
        Result<List<String>> accountResult = accountService.createAccount(form.getCount(), form.getPassword());
        RpcResult result = new RpcResult(accountResult);
        return result;
    }

    @GET
    @Path("/get/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    //todo path中去掉get
    public RpcResult get(@PathParam("address") String address) {
        RpcResult result;
        if (!StringUtils.validAddress(address)) {
            result = RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
            return result;
        }
        Account account = accountService.getAccount(address);
        if (account == null) {
            result = RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        } else {
            result = RpcResult.getSuccess();
            result.setData(new AccountDto(account));
        }
        return result;
    }

    @POST
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    //修改参数传递方式为json
    public RpcResult alias(@FormParam("alias") String alias,
                           @FormParam("address") String address,
                           @FormParam("password") String password) {
        Result result = accountService.setAlias(address, password, alias);
        RpcResult rpcResult = new RpcResult(result);
        return rpcResult;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult accountList() {
        RpcResult<List<AccountDto>> result = RpcResult.getSuccess();
        List<Account> list = accountService.getAccountList();
        List<AccountDto> dtoList = new ArrayList<>();
        for (Account account : list) {
            dtoList.add(new AccountDto(account));
        }
        result.setData(dtoList);
        return result;
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBalance(@PathParam("address") String address) {
        Balance balance = ledgerService.getBalance(address);
        RpcResult result = RpcResult.getSuccess();

        result.setData(new BalanceDto(balance));
        return result;
    }

    @GET
    @Path("/utxo/")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getUtxo(@QueryParam("address") String address,
                             @QueryParam("amount") double amount) {
        if (!StringUtils.validAddress(address) || amount < 0 || amount > Na.TOTAL_VALUE) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        UtxoBalance balance = (UtxoBalance) ledgerService.getBalance(address);
        amount += NulsContext.getInstance().getTxFee().toDouble();

        double usable = 0d;
        boolean enough = false;
        List<OutputDto> dtoList = new ArrayList<>();
        for (int i = 0; i < balance.getUnSpends().size(); i++) {
            UtxoOutput output = balance.getUnSpends().get(i);
            if (output.getStatus() == UtxoOutput.USEABLE) {
                usable += Na.valueOf(output.getValue()).toDouble();
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
    public RpcResult getPrikey(AccountParamForm form) {
        if (!StringUtils.validAddress(form.getAddress()) || !StringUtils.validPassword(form.getPassword())) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        RpcResult result;
        try {
            byte[] prikey = accountService.getPrivateKey(form.getAddress(), form.getPassword());
            result = RpcResult.getSuccess();
            result.setData(Hex.encode(prikey));
        } catch (Exception e) {
            result = RpcResult.getFailed(e.getMessage());
        }
        return result;
    }


    @GET
    @Path("/assets/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAssets(@QueryParam("address") String address) {
        if (!StringUtils.validPassword(address)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        Balance balance = ledgerService.getBalance(address);
        RpcResult result = RpcResult.getSuccess();
        result.setData(new AssetDto("Nuls", balance));
        return result;
    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAddress(@QueryParam("publicKey") String publicKey, @QueryParam("subChainId") Integer subChainId) {
        Address address = new Address((short) subChainId.intValue(), Hex.decode(publicKey));
        RpcResult result = RpcResult.getSuccess();
        result.setData(address.toString());
        return result;
    }

    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult lock(@QueryParam("address") String address, @QueryParam("password") String password,
                          @QueryParam("amount") Double amount, @QueryParam("remark") String remark,
                          @QueryParam("unlockTime") Long unlockTime) {
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
