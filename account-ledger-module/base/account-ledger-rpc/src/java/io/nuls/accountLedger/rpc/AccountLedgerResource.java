/*
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
 *
 */

/**
 * @author: Facjas
 * @date: 2018/5/8
 */
package java.io.nuls.accountLedger.rpc;

import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.accountLedger.constant.AccountLedgerErrorCode;
import io.nuls.accountLedger.service.AccountLedgerService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.nuls.accountLedger.rpc.form.TransferForm;

/**
 * author Facjas
 * date 2018/5/14.
 */

@Path("/accountLedger")
@Api(value = "/browse", description = "Block")
@Component
public class AccountLedgerResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @GET
    @Path("/accountLedger/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户地址查询账户余额", notes = "result.data: balanceJson 返回对应的余额信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Balance.class)
    })
    public Result<Balance> getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                      @PathParam("address") String address) {
        byte[] addressBytes = null;
        try {
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        if (addressBytes.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        Result result = null;
        try {
            result = accountLedgerService.getBalance(addressBytes);
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(AccountLedgerErrorCode.UNKNOW_ERROR);
        }

        if (result == null) {
            return Result.getFailed(AccountLedgerErrorCode.UNKNOW_ERROR);
        }
        return result;
    }

    @POST
    @Path("/accountLedger/transfer/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "转账", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public Result<Balance> transfer(@ApiParam(name = "form", value = "转账", required = true)
                                            TransferForm form) {

        return Result.getSuccess();
    }
}
