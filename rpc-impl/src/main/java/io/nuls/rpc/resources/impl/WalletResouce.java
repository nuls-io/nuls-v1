/**
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
 */
package io.nuls.rpc.resources.impl;

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

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/wallet")
public class WalletResouce {

    private static final int MAX_UNLOCK_TIME = 60;
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @POST
    @Path("/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult unlock(@FormParam("password") String password, @FormParam("unlockTime") Integer unlockTime) {
        AssertUtil.canNotEmpty(password, ErrorCode.NULL_PARAMETER);
        AssertUtil.canNotEmpty(unlockTime);
        if (unlockTime > MAX_UNLOCK_TIME) {
            return RpcResult.getFailed("Unlock time should in a minute!");
        }
        Result result = accountService.unlockAccounts(password, unlockTime);
        return new RpcResult(result);
    }

    @POST
    @Path("/encrypt")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult password(@FormParam("password") String password) {
        Result result = this.accountService.encryptAccount(password);
        return new RpcResult(result);
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult password(@FormParam("password") String password, @FormParam("newPassword") String newPassword) {
        Result result = this.accountService.changePassword(password, newPassword);
        return new RpcResult(result);
    }


    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult transfer(@FormParam("address") String address, @FormParam("password") String password,
                              @FormParam("toAddress") String toAddress, @FormParam("amount") Double amount,
                              @FormParam("remark") String remark) {
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
        //todo
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult importAccount(String priKey) {
        if(StringUtils.isBlank(priKey) || priKey.length() > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        accountService.importAccount(priKey);
        return RpcResult.getSuccess();
    }

}
