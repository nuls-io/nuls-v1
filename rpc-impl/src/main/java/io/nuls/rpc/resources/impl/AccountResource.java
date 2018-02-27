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
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/account")
public class AccountResource {

    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @GET
    @Path("/create/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult create(@QueryParam("count") Integer count) {
        RpcResult result = RpcResult.getSuccess();
        Result<List<String>> accountResult = accountService.createAccount(count);
        result.setData(accountResult.getObject());
        return result;
    }


    @GET
    @Path("/load/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult load(@PathParam("address") String address) {
        RpcResult result = RpcResult.getSuccess();
        Account account = accountService.getAccount(address);
        result.setData(account);
        return result;
    }

    @POST
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult alias(@FormParam("alias") String alias,
                           @FormParam("address") String address,
                           @FormParam("password") String password) {
        RpcResult rpcResult = RpcResult.getSuccess();
        Result result = accountService.setAlias(address, password, alias);
        rpcResult.setData(result);
        return rpcResult;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult accountList() {
        RpcResult<List<Account>> result = RpcResult.getSuccess();
        List<Account> list = accountService.getAccountList();
        result.setData(list);
        return result;
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBalance(@PathParam("address") String address) {
        Balance balance = ledgerService.getBalance(address);
        RpcResult result = RpcResult.getSuccess();
        result.setData(balance);
        return result;
    }

    @GET
    @Path("/prikey/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getPrikey(@PathParam("address") String address, @QueryParam("password") String password) {
        RpcResult result = RpcResult.getSuccess();
        byte[] prikey = accountService.getPrivateKey(address, password);
        result.setData(Hex.encode(prikey));
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
