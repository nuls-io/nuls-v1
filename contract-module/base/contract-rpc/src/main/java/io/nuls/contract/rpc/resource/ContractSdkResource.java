package io.nuls.contract.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.util.CoinDataTool;
import io.nuls.account.util.AccountTool;
import io.nuls.accout.ledger.rpc.dto.TransactionCreatedReturnInfo;
import io.nuls.accout.ledger.rpc.util.LedgerRpcUtil;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.tx.CreateContractTransaction;
import io.nuls.contract.entity.tx.DeleteContractTransaction;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.contract.entity.txdata.DeleteContractData;
import io.nuls.contract.rpc.form.transaction.CallContractTx;
import io.nuls.contract.rpc.form.transaction.CreateContractTx;
import io.nuls.contract.rpc.form.transaction.DeleteContractTx;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.tools.calc.LongUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * contract sdk build transaction
 * <p>
 * Created by wangkun23 on 2018/11/13.
 */

@Path("/contract")
@Api(value = "/contract", description = "contract sdk")
@Component
public class ContractSdkResource {

    @POST
    @Path("/sdk/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create contact transaction")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult createContractTx(
            @ApiParam(name = "createForm", value = "create contact transaction", required = true) CreateContractTx createContractTx) {
        if (!AddressTool.validAddress(createContractTx.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (createContractTx.getGasLimit() < 0 || createContractTx.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (createContractTx.getUtxos().isEmpty()) {
            //utxo not null and size not zero.
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
        }
        Long gasLimit = createContractTx.getGasLimit();
        Long price = createContractTx.getPrice();
        Na value = Na.ZERO;
        long totalGas = LongUtils.mul(gasLimit, price);
        Na totalNa = Na.valueOf(totalGas);
        byte[] senderBytes = AddressTool.getAddress(createContractTx.getSender());
        /**
         * first create a new contract address.
         */
        Address contractAddress = null;
        try {
            contractAddress = AccountTool.createContractAddress();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        byte[] contractAddressBytes = contractAddress.getAddressBytes();
        // 组装txData
        CreateContractData txData = new CreateContractData();
        txData.setSender(senderBytes);
        txData.setContractAddress(contractAddressBytes);
        txData.setValue(value.getValue());
        txData.setGasLimit(gasLimit);
        txData.setPrice(price);
        txData.setCodeLen(createContractTx.getContractCode().length);
        txData.setCode(createContractTx.getContractCode());

        Object[] args = createContractTx.getArgs();
        if (args != null) {
            txData.setArgsCount((byte) args.length);
            if (args.length > 0) {
                txData.setArgs(ContractUtil.twoDimensionalArray(args));
            }
        }
        /**
         * create create contract tx data
         */
        CreateContractTransaction tx = new CreateContractTransaction();
        String remark = createContractTx.getRemark();
        if (StringUtils.isNotBlank(remark)) {
            try {
                tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        tx.setTime(TimeService.currentTimeMillis());
        tx.setTxData(txData);

        CoinDataResult coinDataResult = CoinDataTool.getCoinData(senderBytes, totalNa, tx.size(),
                TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES,
                createContractTx.getUtxos());
        if (!coinDataResult.isEnough()) {
            return RpcClientResult.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
        }
        /**
         * build coin data
         */
        CoinData coinData = new CoinData();
        coinData.setFrom(coinDataResult.getCoinList());
        if (coinDataResult.getChange() != null) {
            coinData.getTo().add(coinDataResult.getChange());
        }
        // transfer to contract address.
        if (value.isGreaterThan(Na.ZERO)) {
            Coin toCoin = new Coin(contractAddressBytes, value);
            coinData.getTo().add(toCoin);
        }
        tx.setCoinData(coinData);

        return this.buildReturnInfo(tx);
    }

    @POST
    @Path("/sdk/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "call smart contract transaction")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult callContractTx(
            @ApiParam(name = "callForm", value = "call smart contract transaction", required = true) CallContractTx callContractTx) {

        if (!AddressTool.validAddress(callContractTx.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String contractAddress = callContractTx.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (callContractTx.getGasLimit() < 0 || callContractTx.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isNotBlank(callContractTx.getMethodName())) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (callContractTx.getUtxos().isEmpty()) {
            //utxo not null and size not zero.
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
        }
        Long gasLimit = callContractTx.getGasLimit();
        Long price = callContractTx.getPrice();
        Na value = callContractTx.getValue();
        byte[] senderBytes = AddressTool.getAddress(callContractTx.getSender());
        byte[] contractAddressBytes = AddressTool.getAddress(callContractTx.getContractAddress());
        if (value == null) {
            value = Na.ZERO;
        }
        CallContractTransaction tx = new CallContractTransaction();
        String remark = callContractTx.getRemark();
        if (StringUtils.isNotBlank(remark)) {
            try {
                tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
                throw new RuntimeException(e);
            }
        }
        tx.setTime(TimeService.currentTimeMillis());
        long gasUsed = gasLimit.longValue();
        Na imputedNa = Na.valueOf(LongUtils.mul(gasUsed, price));
        Na totalNa = imputedNa.add(value);

        CallContractData callContractData = new CallContractData();
        callContractData.setContractAddress(contractAddressBytes);
        callContractData.setSender(senderBytes);
        callContractData.setValue(value.getValue());
        callContractData.setPrice(price.longValue());
        callContractData.setGasLimit(gasLimit.longValue());
        callContractData.setMethodName(callContractTx.getMethodName());
        callContractData.setMethodDesc(callContractTx.getMethodDesc());
        /**
         * build args
         */
        Object[] args = callContractTx.getArgs();
        if (args != null) {
            callContractData.setArgsCount((byte) args.length);
            callContractData.setArgs(ContractUtil.twoDimensionalArray(args));
        }
        tx.setTxData(callContractData);

        CoinDataResult coinDataResult = CoinDataTool.getCoinData(senderBytes, totalNa, tx.size(),
                TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES,
                callContractTx.getUtxos());
        if (!coinDataResult.isEnough()) {
            return RpcClientResult.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
        }
        /**
         * build coin data
         */
        CoinData coinData = new CoinData();
        coinData.setFrom(coinDataResult.getCoinList());
        if (coinDataResult.getChange() != null) {
            coinData.getTo().add(coinDataResult.getChange());
        }
        // transfer to contract address.
        if (value.isGreaterThan(Na.ZERO)) {
            Coin toCoin = new Coin(contractAddressBytes, value);
            coinData.getTo().add(toCoin);
        }
        tx.setCoinData(coinData);
        return this.buildReturnInfo(tx);
    }


    @POST
    @Path("/sdk/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete smart contract transaction")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult deleteContractTx(
            @ApiParam(name = "deleteForm", value = "delete smart contract transaction", required = true) DeleteContractTx deleteContractTx) {
        if (!AddressTool.validAddress(deleteContractTx.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        String contractAddress = deleteContractTx.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (deleteContractTx.getUtxos().isEmpty()) {
            //utxo not null and size not zero.
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
        }
        DeleteContractTransaction tx = new DeleteContractTransaction();
        if (StringUtils.isNotBlank(deleteContractTx.getRemark())) {
            try {
                tx.setRemark(deleteContractTx.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        tx.setTime(TimeService.currentTimeMillis());

        byte[] senderBytes = AddressTool.getAddress(deleteContractTx.getSender());
        byte[] contractAddressBytes = AddressTool.getAddress(deleteContractTx.getContractAddress());

        /**
         * build tx data
         */
        DeleteContractData deleteContractData = new DeleteContractData();
        deleteContractData.setContractAddress(contractAddressBytes);
        deleteContractData.setSender(senderBytes);
        tx.setTxData(deleteContractData);

        CoinDataResult coinDataResult = CoinDataTool.getCoinData(senderBytes, Na.ZERO, tx.size(),
                TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES,
                deleteContractTx.getUtxos());
        if (!coinDataResult.isEnough()) {
            return RpcClientResult.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
        }
        /**
         * build coin data
         */
        CoinData coinData = new CoinData();
        coinData.setFrom(coinDataResult.getCoinList());
        if (coinDataResult.getChange() != null) {
            coinData.getTo().add(coinDataResult.getChange());
        }
        tx.setCoinData(coinData);

        return this.buildReturnInfo(tx);
    }

    private RpcClientResult buildReturnInfo(Transaction tx) {
        try {
            TransactionCreatedReturnInfo returnInfo = LedgerRpcUtil.makeReturnInfo(tx);
            Map<String, TransactionCreatedReturnInfo> data = new HashMap<>();
            data.put("value", returnInfo);
            return Result.getSuccess().setData(data).toRpcClientResult();
        } catch (IOException e) {
            Log.error(e);
            return RpcClientResult.getFailed(e.getMessage());
        }
    }
}
