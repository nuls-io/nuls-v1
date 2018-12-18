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
package io.nuls.ledger.rpc.resource;

import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.model.Entry;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.rpc.model.*;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.ledger.storage.util.CoinComparator;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @desription:
 * @author: PierreLuo
 */
@Path("/utxo")
@Api(value = "/utxo", description = "utxo")
@Component
public class UtxoResource {

    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;

    @GET
    @Path("/limit/{address}/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据address和limit查询UTXO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountUtxoDto.class)
    })
    public RpcClientResult getUtxoByAddressAndLimit(
            @ApiParam(name = "address", value = "地址", required = true) @PathParam("address") String address,
            @ApiParam(name = "limit", value = "数量", required = true) @PathParam("limit") Integer limit) {
        if (StringUtils.isBlank(address) || limit == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = null;
        try {
            List<Coin> coinList = getAllUtxoByAddress(address);
            int limitValue = limit.intValue();
            boolean isLoadAll = (limitValue == 0);
            AccountUtxoDto accountUtxoDto = new AccountUtxoDto();
            List<UtxoDto> list = new LinkedList<>();
            int i = 0;
            for (Coin coin : coinList) {
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                if (!isLoadAll) {
                    if (i >= limitValue) {
                        break;
                    }
                    i++;
                }
                list.add(new UtxoDto(coin));
            }
            accountUtxoDto.setUtxoDtoList(list);
            result = Result.getSuccess().setData(accountUtxoDto);
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
            return result.toRpcClientResult();
        }
    }

    @GET
    @Path("/amount/{address}/{amount}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据address和amount查询UTXO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = AccountUtxoDto.class)
    })
    public RpcClientResult getUtxoByAddressAndAmount(
            @ApiParam(name = "address", value = "地址", required = true) @PathParam("address") String address,
            @ApiParam(name = "amount", value = "金额", required = true) @PathParam("amount") Long amount) {
        if (StringUtils.isBlank(address) || amount == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = null;
        try {
            List<Coin> coinList = getAllUtxoByAddress(address);
            Na amountNa = Na.valueOf(amount.longValue());

            AccountUtxoDto accountUtxoDto = new AccountUtxoDto();
            List<UtxoDto> list = new LinkedList<>();
            Na values = Na.ZERO;
            for (Coin coin : coinList) {
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                list.add(new UtxoDto(coin));
                values = values.add(coin.getNa());

                if (values.isGreaterOrEquals(amountNa)) {
                    break;
                }
            }
            accountUtxoDto.setUtxoDtoList(list);
            result = Result.getSuccess().setData(accountUtxoDto);
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
            return result.toRpcClientResult();
        }
    }

    private List<Coin> getAllUtxoByAddress(String address) {
        List<Coin> coinList = new ArrayList<>();
        byte[] addressBytes = AddressTool.getAddress(address);
        List<Entry<byte[], byte[]>> coinBytesList = utxoLedgerUtxoStorageService.getAllUtxoEntryBytes();
        Coin coin;
        for (Entry<byte[], byte[]> coinEntryBytes : coinBytesList) {
            coin = new Coin();
            try {
                coin.parse(coinEntryBytes.getValue(), 0);
            } catch (NulsException e) {
                Log.info("parse coin form db error");
                continue;
            }
            //if (Arrays.equals(coin.(), addressBytes))
            if (Arrays.equals(coin.getAddress(), addressBytes)) {
                coin.setOwner(coinEntryBytes.getKey());
                coinList.add(coin);
            }
        }
        Collections.sort(coinList, CoinComparator.getInstance());
        return coinList;
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询代币情况")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = TokenInfoDto.class)
    })
    public RpcClientResult getInfo() throws NulsException {
        long height = NulsContext.getInstance().getBestHeight();
        List<Entry<byte[], byte[]>> coinBytesList = utxoLedgerUtxoStorageService.getAllUtxoEntryBytes();
        double totalNuls = 0d;
        double lockedNuls = 0d;
        Map<String, Holder> map = new HashMap<>();
        Coin coin = new Coin();
        int index = 0;
        for (Entry<byte[], byte[]> coinEntryBytes : coinBytesList) {
            coin.parse(coinEntryBytes.getValue(), 0);
            double value = coin.getNa().toDouble();
            String address = AddressTool.getStringAddressByBytes(coin.getOwner());
            Holder holder = map.get(address);
            if (null == holder) {
                holder = new Holder();
                holder.setAddress(address);
                map.put(address, holder);
            }
            holder.addTotal(value);
            totalNuls = DoubleUtils.sum(totalNuls, value);
            if (coin.getLockTime() == -1 || coin.getLockTime() > System.currentTimeMillis() || (coin.getLockTime() < 1531152000000L && coin.getLockTime() > height)) {
                holder.addLocked(value);
                lockedNuls = DoubleUtils.sum(lockedNuls, value);
            }
            System.out.println(index++);
        }
        Result<TokenInfoDto> result = Result.getSuccess();
        TokenInfoDto info = new TokenInfoDto();
        info.setTotalNuls(DoubleUtils.getRoundStr(totalNuls, 8, true));
        info.setLockedNuls(DoubleUtils.getRoundStr(lockedNuls, 8, true));
        List<Holder> holderList = new ArrayList<>(map.values());
        Collections.sort(holderList);
        List<HolderDto> dtoList = new ArrayList<>();
        for (Holder holder : holderList) {
            HolderDto dto = new HolderDto(holder);
            dtoList.add(dto);
        }
        info.setAddressList(dtoList);
        result.setData(info);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/totalCoins")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询代币总量")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = TokenInfoDto.class)
    })
    public RpcClientResult getTotalCoins() throws NulsException {
        Result<NulsInfoDto> result = Result.getSuccess();
        NulsInfoDto info = new NulsInfoDto();
        info.setTotalNuls(NulsContext.totalNuls);
        info.setLockedNuls(NulsContext.lockedNuls);
        result.setData(info);
        return result.toRpcClientResult();
    }
}
