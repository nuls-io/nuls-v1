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
package io.nuls.utxo.accounts.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.utxo.accounts.constant.UtxoAccountsErrorCode;
import io.nuls.utxo.accounts.rpc.dto.AccountBalanceDto;
import io.nuls.utxo.accounts.storage.po.LockedBalance;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsBalancePo;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;


@Path("/utxoAccounts")
@Api(value = "utxoAccounts", description = "utxoAccounts")
@Component
public class UtxoAccountsResource {
    @Autowired
    private UtxoAccountsStorageService utxoAccountsStorageService;
    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("[查询] 查询账户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult get(@ApiParam(name = "address", value = "账户地址", required = true)
                               @PathParam("address") String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        try {
            Result<UtxoAccountsBalancePo> utxoAccountsBalance=utxoAccountsStorageService.getUtxoAccountsBalanceByAddress(AddressTool.getAddress(address));
            long synBlockHeight=utxoAccountsStorageService.getHadSynBlockHeight();
            if(null==utxoAccountsBalance || null==utxoAccountsBalance.getData()){
                return Result.getFailed(UtxoAccountsErrorCode.DATA_NOT_FOUND).toRpcClientResult();
            }
            UtxoAccountsBalancePo dbAccountsBalance =utxoAccountsBalance.getData();
            AccountBalanceDto accountBalance=new AccountBalanceDto();
            accountBalance.setAddress(address);
            long totalNa=dbAccountsBalance.getOutputBalance()-(dbAccountsBalance.getInputBalance());
            totalNa+=dbAccountsBalance.getContractToBalance();
            totalNa-=dbAccountsBalance.getContractFromBalance();
            accountBalance.setNuls( new BigDecimal(totalNa).toPlainString());
            long timeLockedNa=0;
            long heightLockedNa=0;
            long permanentLockedNa=dbAccountsBalance.getLockedPermanentBalance()-(dbAccountsBalance.getUnLockedPermanentBalance());
            long lockedNa=permanentLockedNa;
            List<LockedBalance> timeLockedBalance=dbAccountsBalance.getLockedTimeList();
            long currentTime=TimeService.currentTimeMillis();
            for(LockedBalance balance:timeLockedBalance){
                if(balance.getLockedTime()>currentTime){
                    lockedNa+=balance.getLockedBalance();
                    timeLockedNa+=balance.getLockedBalance();
                    accountBalance.getLockedTimeList().add(balance);
                }else{
                    break;
                }
            }
            List<LockedBalance> heightLockedBalance=dbAccountsBalance.getLockedHeightList();
            for(LockedBalance balance:heightLockedBalance){
                if(balance.getLockedTime()>synBlockHeight){
                    lockedNa+=balance.getLockedBalance();
                    heightLockedNa+=balance.getLockedBalance();
                    accountBalance.getLockedHeightList().add(balance);
                }else{
                    break;
                }
            }
            accountBalance.setPermanentLocked(new BigDecimal(permanentLockedNa).toPlainString());
            accountBalance.setLocked(new BigDecimal(lockedNa).toPlainString());
            accountBalance.setTimeLocked(new BigDecimal(timeLockedNa).toPlainString());
            accountBalance.setHeightLocked(new BigDecimal(heightLockedNa).toPlainString());
            accountBalance.setSynBlockHeight(String.valueOf(synBlockHeight));
            long netHeight= NulsContext.getInstance().getNetBestBlockHeight();
            accountBalance.setNetBlockHeight(String.valueOf(netHeight));
            accountBalance.setContractIn(String.valueOf(dbAccountsBalance.getContractToBalance()));
            accountBalance.setContractOut(String.valueOf(dbAccountsBalance.getContractFromBalance()));
            return Result.getSuccess().setData(accountBalance).toRpcClientResult();
        } catch (NulsException e) {
            Log.error(e);
        }
        return Result.getFailed(UtxoAccountsErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
    }
}
