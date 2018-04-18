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

import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.MD5Util;
import io.nuls.core.model.Result;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Na;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.form.AccountParamForm;
import io.nuls.rpc.resources.form.TransferForm;
import io.nuls.rpc.resources.form.WalletPasswordForm;
import io.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/wallet")
@Api(value = "/browse", description = "Wallet")
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
        if (result.isSuccess()) {
            NulsContext.setCachedPasswordOfWallet(password);
        }
        return new RpcResult(result);
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "重置钱包密码", notes = "Nuls_RPC_API文档[3.4.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = RpcResult.class)
    })
    public RpcResult password(@ApiParam(name = "重置钱包密码表单数据", value = "JSONFormat", required = true)
                                          WalletPasswordForm form) {
        Result result = this.accountService.changePassword(form.getPassword(), form.getNewPassword());
        if (result.isSuccess()) {
            NulsContext.setCachedPasswordOfWallet(form.getNewPassword());
        }
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
        if (StringUtils.isNotBlank(form.getAddress()) && !Address.validAddress(form.getAddress())) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(form.getPassword())) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        Result result;
        if (StringUtils.isNotBlank(form.getAddress())) {
            result = accountService.exportAccount(form.getAddress(), form.getPassword());
        } else {
            result = this.accountService.exportAccounts(form.getPassword());
        }
        return new RpcResult(result);
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
        NulsContext.setCachedPasswordOfWallet(form.getPassword());

        Result result = null;
        try {
            result = accountService.importAccount(form.getPrikey(), form.getPassword());
        } catch (Exception e) {
            return RpcResult.getFailed(result.getErrorCode());
        }
        return new RpcResult(result);
    }

    @POST
    @Path("/imports")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RpcResult importAccountFile(@FormDataParam("file") InputStream in,
                                       @FormDataParam("file") FormDataContentDisposition disposition,
                                       @FormDataParam("password") String password) {
        if (in == null || disposition == null || !StringUtils.validPassword(password)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        String fileName = disposition.getFileName();
        if (!fileName.endsWith(".nuls")) {
            return RpcResult.getFailed("File suffix name is wrong");
        }

        if (!StringUtils.validPassword(password)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> map = getFileContent(in);
        if (map == null) {
            return RpcResult.getFailed(ErrorCode.FILE_BROKEN);
        }

        List<String> keys = (List<String>) map.get("keys");
        String md5 = (String) map.get("password");
        if (keys == null || keys.isEmpty() || StringUtils.isBlank(md5)) {
            return RpcResult.getFailed(ErrorCode.FILE_BROKEN);
        }

        if (!MD5Util.md5(password).equals(md5)) {
            return RpcResult.getFailed(ErrorCode.PASSWORD_IS_WRONG);
        }

        Result result = accountService.importAccounts(keys, password);

        RpcResult rpcResult = new RpcResult(result);

        return rpcResult;
    }


    private Map<String, Object> getFileContent(InputStream in) {
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        Map<String, Object> map = null;
        try {
            read = new InputStreamReader(in, NulsConfig.DEFAULT_ENCODING);
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            StringBuffer buffer = new StringBuffer();
            while ((lineTxt = bufferedReader.readLine()) != null) {
                buffer.append(lineTxt);
            }
            map = JSONUtils.json2map(buffer.toString());
        } catch (Exception e) {
            return null;
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return map;
    }

    @POST
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult removeAccount(AccountParamForm form) {
        if (!StringUtils.validPassword(form.getPassword()) || !Address.validAddress(form.getAddress())) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        Result result = accountService.removeAccount(form.getAddress(), form.getPassword());
        return new RpcResult(result);
    }
}
