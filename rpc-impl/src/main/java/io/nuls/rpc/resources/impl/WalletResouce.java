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
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/wallet")
public class WalletResouce {

    private static final int MAX_UNLOCK_TIME = 60;
    private NulsContext context = NulsContext.getInstance();
    private AccountService accountService = context.getService(AccountService.class);
    private LedgerService ledgerService = context.getService(LedgerService.class);

    @POST
    @Path("/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult unlock(@FormParam("password") String password, @FormParam("unlockSeconds") Integer unlockSeconds) {
        AssertUtil.canNotEmpty(password, ErrorCode.NULL_PARAMETER);
        AssertUtil.canNotEmpty(unlockSeconds);
        if (unlockSeconds > MAX_UNLOCK_TIME) {
            return RpcResult.getFailed("Unlock time should in a minute!");
        }
        Result result = accountService.unlockAccounts(password, unlockSeconds);
        return new RpcResult(result);
    }

    @POST
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult password(@FormParam("password") String password) {
        AssertUtil.canNotEmpty(password);
        boolean formatCheck = StringUtils.validPassword(password);
        if (!formatCheck) {
            return RpcResult.getFailed("Password format must conform to specification");
        }
        Result result = this.accountService.encryptAccount(password);
        return new RpcResult(result);
    }

    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult password(@FormParam("password") String password, @FormParam("newPassword") String newPassword) {
        AssertUtil.canNotEmpty(newPassword);
        boolean formatCheck = StringUtils.validPassword(newPassword);
        if (!formatCheck) {
            return RpcResult.getFailed("New password format must conform to specification");
        }
        Result result = this.accountService.changePassword(password, newPassword);
        return new RpcResult(result);
    }

    @GET
    @Path("/account/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult accountList() {
        RpcResult<List<Account>> result = RpcResult.getSuccess();
        List<Account> list = accountService.getAccountList();
        result.setData(list);
        return result;
    }

    @POST
    @Path("/account/{address}/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult transfer(@PathParam("address") String address, @FormParam("password") String password,
                              @FormParam("toAddress") String toAddress, @FormParam("amount") Double amount,
                              @FormParam("remark") String remark) {
        AssertUtil.canNotEmpty(address);
        AssertUtil.canNotEmpty(toAddress);
        AssertUtil.canNotEmpty(amount);
        Result result = this.ledgerService.transfer(address, password, toAddress, Na.parseNuls(amount), remark);
        return new RpcResult(result);
    }

    @POST
    @Path("/backup")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult backup(@FormParam("address") String address, @FormParam("password") String password) {
        AssertUtil.canNotEmpty(address);
        //todo Result result = this.accountService.exportAccount(address);
        return new RpcResult();
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult importWallet(File file) {
        AssertUtil.canNotEmpty(file);
        //todo
        return RpcResult.getSuccess();
    }

}
