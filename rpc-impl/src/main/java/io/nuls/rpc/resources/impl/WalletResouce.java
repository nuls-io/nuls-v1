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

import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.form.AccountParamForm;
import io.nuls.rpc.resources.form.TransferForm;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;

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
    public RpcResult transfer(TransferForm form) {
        AssertUtil.canNotEmpty(form.getToAddress());
        AssertUtil.canNotEmpty(form.getAmount());
        Result result = this.ledgerService.transfer(form.getAddress(), form.getPassword(),
                form.getToAddress(), Na.valueOf(form.getAmount()), form.getRemark());
        return new RpcResult(result);
    }

    @POST
    @Path("/backup")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult backup(AccountParamForm form) {
        Result result = this.accountService.exportAccounts(form.getPassword());
        return new RpcResult(result);
    }

    @POST
    @Path("/imports")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RpcResult importAccountFile(@FormDataParam("file") InputStream in,
                                       @FormDataParam("file") FormDataContentDisposition disposition,
                                       @FormDataParam("password") String password) {
        String fileName = disposition.getFileName();
        if (!fileName.endsWith(".nuls")) {
            return RpcResult.getFailed("File suffix name is wrong");
        }

        if(!StringUtils.validPassword(password)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(in, NulsContext.DEFAULT_ENCODING);
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            StringBuffer buffer = new StringBuffer();
            while ((lineTxt = bufferedReader.readLine()) != null) {
                buffer.append(lineTxt);
            }
            String content = buffer.toString();
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        RpcResult rpcResult = RpcResult.getSuccess();
        rpcResult.setData(fileName);

        return rpcResult;
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult importAccount(AccountParamForm form) {
        if (!StringUtils.validPassword(form.getPassword()) ||
                StringUtils.isBlank(form.getPrikey()) ||
                form.getPrikey().length() > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        Result result = accountService.importAccount(form.getPrikey(), form.getPassword());
        return new RpcResult(result);
    }
}
