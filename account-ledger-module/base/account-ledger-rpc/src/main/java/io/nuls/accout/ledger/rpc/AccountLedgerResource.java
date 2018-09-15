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
 */
package io.nuls.accout.ledger.rpc;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.base.service.LocalUtxoService;
import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.account.ledger.constant.AccountLedgerConstant;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.MultipleAddressTransferModel;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.accout.ledger.rpc.dto.*;
import io.nuls.accout.ledger.rpc.form.*;
import io.nuls.accout.ledger.rpc.util.UtxoDtoComparator;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.crypto.AESEncrypt;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Exception.CryptoException;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.constant.TxStatusEnum;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.model.tx.TransferTransaction;
import io.swagger.annotations.*;
import org.spongycastle.util.Arrays;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * author Facjas
 * date 2018/5/14.
 */

@Path("/accountledger")
@Api(value = "/accountledger", description = "accountledger")
@Component
public class AccountLedgerResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LocalUtxoService localUtxoService;

    @Autowired
    private ContractService contractService;

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户地址查询账户余额", notes = "result.data: balanceJson 返回对应的余额信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Balance.class)
    })
    public RpcClientResult getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                      @PathParam("address") String address) {
        byte[] addressBytes = null;
        try {
            addressBytes = AddressTool.getAddress(address);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountLedgerService.getBalance(addressBytes);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "转账", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transfer(@ApiParam(name = "form", value = "转账", required = true) TransferForm form) {
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getToAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (form.getAmount() <= 0) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        byte[] remarkBytes;
        try {
            remarkBytes = form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Na value = Na.valueOf(form.getAmount());

        Result result = accountLedgerService.transfer(AddressTool.getAddress(form.getAddress()),
                AddressTool.getAddress(form.getToAddress()),
                value, form.getPassword(), remarkBytes, TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/changeWhole")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "零钱汇整", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult changeWhole(@ApiParam(name = "form", value = "零钱汇整", required = true) ChangeToWholeTransactionForm form) {
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountLedgerService.changeWhole(AddressTool.getAddress(form.getAddress()), form.getPassword(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/dapp")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建并广播DAPP交易", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult dapp(@ApiParam(name = "form", value = "DAPP交易", required = true) DataTransactionForm form) {
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getData())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        byte[] data;
        try {
            data = form.getData().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        byte[] remarkBytes;
        try {
            remarkBytes = form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = accountLedgerService.dapp(AddressTool.getAddress(form.getAddress()), form.getPassword(), data, remarkBytes);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/multipleAddressTransfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "多地址转账（from多个地址的密码必须一样）", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult multipleAddressTransfer(@ApiParam(name = "form", value = "创建多账户转账交易", required = true)
                                                           MulitpleTransactionForm form) {
        List<MultipleAddressTransferModel> fromModelList = new ArrayList<>();
        List<MultipleAddressTransferModel> toModelList = new ArrayList<>();
        if (form.getInputs() == null || form.getOutputs() == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        for (MulipleTxFromDto from : form.getInputs()) {
            MultipleAddressTransferModel model = new MultipleAddressTransferModel();
            if (!AddressTool.validAddress(from.getAddress())) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            if (from.getAmount() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            model.setAddress(AddressTool.getAddress(from.getAddress()));
            model.setAmount(from.getAmount());
            fromModelList.add(model);
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        for (MultipleTxToDto to : form.getOutputs()) {
            MultipleAddressTransferModel model = new MultipleAddressTransferModel();
            if (!AddressTool.validAddress(to.getToAddress())) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            model.setAddress(AddressTool.getAddress(to.getToAddress()));
            model.setAmount(to.getAmount());
            toModelList.add(model);
        }
        Result result = accountLedgerService.multipleAddressTransfer(fromModelList, toModelList, form.getPassword(), form.getRemark(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @GET
    @Path("/estimateFee/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "预估手续费—做零钱换整功能", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult estimateFee(@ApiParam(name = "address", value = "预估手续费—做零钱换整功能", required = true)
                                       @PathParam("address") String address) {
        if (address == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountLedgerService.estimateFee(AddressTool.getAddress(address), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        Long fee = null;
        if (result.isSuccess()) {
            fee = ((Na) result.getData()).getValue();
        }
        Map<String, Long> map = new HashMap<>();
        map.put("fee", fee);
        result.setData(map);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/getTotalUTXO/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取总额和多少条utxo—做零钱换整功能", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult getTotalUTXO(@ApiParam(name = "address", value = "获取总额和多少条utxo—做零钱换整功能", required = true)
                                        @PathParam("address") String address) {
        if (address == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result result = accountLedgerService.getAvailableTotalUTXO(AddressTool.getAddress(address));
        if (!result.isSuccess()) {
            return Result.getFailed(AccountErrorCode.FAILED).toRpcClientResult();
        }
        return result.toRpcClientResult();
    }

    @GET
    @Path("/transfer/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "转账手续费", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transferFee(@BeanParam() TransferFeeForm form) {
        if (form == null) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getToAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (form.getAmount() <= 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Na value = Na.valueOf(form.getAmount());
        Result result = accountLedgerService.transferFee(AddressTool.getAddress(form.getAddress()),
                AddressTool.getAddress(form.getToAddress()), value, form.getRemark(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        Transaction tx = new TransferTransaction();
        try {
            tx.setRemark(form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        tx.setTime(TimeService.currentTimeMillis());
        CoinData coinData = new CoinData();
        Coin toCoin = new Coin(AddressTool.getAddress(form.getToAddress()), value);
        coinData.getTo().add(toCoin);
        tx.setCoinData(coinData);
        Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(form.getAddress()), tx,
                TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
        Map<String, Long> map = new HashMap<>();
        Long fee = null;
        Long maxAmount = null;
        if (result.isSuccess()) {
            fee = ((Na) result.getData()).getValue();
        }
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", fee);
        map.put("maxAmount", maxAmount);
        result.setData(map);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/transaction")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建交易", notes = "result.data: resultJson 返回交易对象")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult createTransaction(@ApiParam(name = "form", value = "导入交易输入输出", required = true)
                                                     TransactionForm form) {
        if (form.getInputs() == null || form.getInputs().isEmpty()) {
            return RpcClientResult.getFailed("inputs error");
        }
        if (form.getOutputs() == null || form.getOutputs().isEmpty()) {
            return RpcClientResult.getFailed("outputs error");
        }

        byte[] remark = null;
        if (!StringUtils.isBlank(form.getRemark())) {
            try {
                remark = form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return RpcClientResult.getFailed("remark error");
            }
        }

        List<Coin> outputs = new ArrayList<>();
        for (int i = 0; i < form.getOutputs().size(); i++) {
            OutputDto outputDto = form.getOutputs().get(i);
            Coin to = new Coin();
            try {
                to.setOwner(AddressTool.getAddress(outputDto.getAddress()));
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            try {
                to.setNa(Na.valueOf(outputDto.getValue()));
            } catch (Exception e) {
                return Result.getFailed(LedgerErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
            }

            if (outputDto.getLockTime() < 0) {
                return RpcClientResult.getFailed("lockTime error");
            }

            to.setLockTime(outputDto.getLockTime());
            outputs.add(to);
        }

        List<Coin> inputs = new ArrayList<>();
        for (int i = 0; i < form.getInputs().size(); i++) {
            InputDto inputDto = form.getInputs().get(i);
            byte[] key = Arrays.concatenate(Hex.decode(inputDto.getFromHash()), new VarInt(inputDto.getFromIndex()).encode());
            Coin coin = new Coin();
            coin.setOwner(key);
            coin.setLockTime(inputDto.getLockTime());
            coin.setNa(Na.valueOf(inputDto.getValue()));
            inputs.add(coin);
        }
        Result result = accountLedgerService.createTransaction(inputs, outputs, remark);
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/transaction/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "交易签名", notes = "result.data: resultJson 返回交易对象")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult signTransaction(@ApiParam(name = "form", value = "交易信息", required = true)
                                                   TransactionHexForm form) {
        if (StringUtils.isBlank(form.getPriKey())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (StringUtils.isBlank(form.getTxHex())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        String priKey = form.getPriKey();
        if (StringUtils.isNotBlank(form.getPassword())) {
            if (StringUtils.validPassword(form.getPassword())) {
                //decrypt
                byte[] privateKeyBytes = null;
                try {
                    privateKeyBytes = AESEncrypt.decrypt(Hex.decode(priKey), form.getPassword());
                } catch (CryptoException e) {
                    return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
                }
                priKey = Hex.encode(privateKeyBytes);
            } else {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
        }

        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        //is private key matches address
        ECKey key = ECKey.fromPrivate(new BigInteger(1, Hex.decode(priKey)));
        try {
            String newAddress = AccountTool.newAddress(key).getBase58();
            if (!newAddress.equals(form.getAddress())) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        try {
            byte[] data = Hex.decode(form.getTxHex());
            Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(data));
            tx = accountLedgerService.signTransaction(tx, key);

//            Result validateResult = tx.verify();
//            if (validateResult.isFailed()) {
//                return Result.getFailed(validateResult.getErrorCode()).toRpcClientResult();
//            }

//            for (Coin coin : tx.getCoinData().getFrom()) {
//                Coin utxo = ledgerService.getUtxo(coin.());
//                if (utxo == null) {
//                    return Result.getFailed(LedgerErrorCode.UTXO_NOT_FOUND).toRpcClientResult();
//                }
//
//                if (!form.getAddress().equals(AddressTool.getStringAddressByBytes(utxo.()))) {
//                    return Result.getFailed(LedgerErrorCode.INVALID_INPUT).toRpcClientResult();
//                }
//
//            }

            Map<String, String> map = new HashMap<>();
            map.put("value", Hex.encode(tx.serialize()));
            return Result.getSuccess().setData(map).toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
        }
    }

    @POST
    @Path("/transaction/broadcast")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "广播交易", notes = "result.data: resultJson 返回交易对象")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult broadcast(@ApiParam(name = "form", value = "交易信息", required = true) BroadHexTxForm form) {
        if (StringUtils.isBlank(form.getTxHex())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        try {
            byte[] data = Hex.decode(form.getTxHex());
            Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(data));
            Result result = accountLedgerService.broadcast(tx);
            if (result.isSuccess()) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", tx.getHash().getDigestHex());
                result.setData(map);
            }
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.DATA_PARSE_ERROR).toRpcClientResult();

        }
    }

    @POST
    @Path("/transaction/valiTransaction")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证交易是否正确", notes = "result.data: resultJson 返回验证结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult valiTransaction(@ApiParam(name = "form", value = "验证交易是否正确", required = true) TransactionHexForm form) {
        if (StringUtils.isBlank(form.getTxHex())) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        try {
            byte[] data = Hex.decode(form.getTxHex());
            Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(data));
            ValidateResult validateResult = tx.verify();
            if (validateResult.isFailed()) {
                return Result.getFailed(validateResult.getErrorCode()).toRpcClientResult();
            }
            validateResult = this.ledgerService.verifyCoinData(tx, new HashMap<>(), new HashSet<>());
            if (validateResult.isFailed() && !validateResult.getErrorCode().equals(TransactionErrorCode.ORPHAN_TX)) {
                return Result.getFailed(validateResult.getErrorCode()).toRpcClientResult();
            }
            Result result = Result.getSuccess();
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.DATA_PARSE_ERROR).toRpcClientResult();

        }
    }

    @GET
    @Path("/tx/list/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户地址查询交易列表", notes = "result.data: balanceJson 返回账户相关的交易列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getTxInfoList(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address,
                                         @ApiParam(name = "assetType", value = "资产类型")
                                         @QueryParam("assetType") String assetType,
                                         @ApiParam(name = "type", value = "类型")
                                         @QueryParam("type") Integer type,
                                         @ApiParam(name = "pageNumber", value = "页码")
                                         @QueryParam("pageNumber") Integer pageNumber,
                                         @ApiParam(name = "pageSize", value = "每页条数")
                                         @QueryParam("pageSize") Integer pageSize) {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (type == null || type <= 0) {
            type = -1;
        }

        byte[] addressBytes = null;
        Result dtoResult = Result.getSuccess();

        try {
            addressBytes = AddressTool.getAddress(address.trim());
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        List<TransactionInfo> result = new ArrayList<TransactionInfo>();

        boolean isEmptyAssetType = StringUtils.isBlank(assetType);
        boolean isNeedQueryToken = StringUtils.isBlank(assetType) || AddressTool.validAddress(assetType);
        boolean isNeedQueryNuls = StringUtils.isBlank(assetType) || "NULS".equals(assetType);

        Set<String> hashCheckSet = MapUtil.createHashSet(8);
        // 合约token代币转账交易记录
        if (isNeedQueryToken) {
            Result<List<ContractTokenTransferInfoPo>> listResult = contractService.getTokenTransferInfoList(address);

            List<ContractTokenTransferInfoPo> list = listResult.getData();
            if (list != null && list.size() > 0) {
                List<ContractTokenTransferInfoPo> tokenInfoList = null;
                if (!isEmptyAssetType) {
                    String contractAddress = assetType;
                    if (AddressTool.validAddress(contractAddress)) {
                        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
                        tokenInfoList = list.stream().filter(po -> Arrays.areEqual(contractAddressBytes, po.getContractAddress())).collect(Collectors.toList());
                    }
                } else {
                    tokenInfoList = list;
                }
                TransactionInfo info = null;
                for (ContractTokenTransferInfoPo po : tokenInfoList) {
                    info = new TransactionInfo();
                    // 临时type，用于区分资产类型 - 基础币和代币
                    info.setTxType(1000);
                    NulsDigestData hashData = new NulsDigestData();
                    try {
                        hashData.parse(po.getTxHash(), 0);
                    } catch (NulsException e) {
                        Log.error(e);
                        //skip it
                    }
                    info.setTxHash(hashData);
                    info.setContractAddress(po.getContractAddress());
                    info.setTime(po.getTime());
                    info.setBlockHeight(po.getBlockHeight());
                    info.setStatus(po.getStatus());
                    info.setInfo(po.getInfo(addressBytes));
                    info.setSymbol(po.getSymbol());
                    result.add(info);
                    hashCheckSet.add(hashData.getDigestHex());
                }
            }
        }

        // 基础币交易记录
        if (isNeedQueryNuls) {
            Result<List<TransactionInfo>> rawResult = accountLedgerService.getTxInfoList(addressBytes);
            if (rawResult.isFailed()) {
                dtoResult.setSuccess(false);
                dtoResult.setErrorCode(rawResult.getErrorCode());
                return dtoResult.toRpcClientResult();
            }

            List<TransactionInfo> infoList = rawResult.getData();
            if (infoList != null && infoList.size() > 0) {
                // 过滤掉既是调用合约的交易类型，又是代币转账类型的交易
                List<TransactionInfo> baseList = infoList.stream().filter(info -> hashCheckSet.add(info.getTxHash().getDigestHex())).collect(Collectors.toList());

                if (type == -1) {
                    result.addAll(baseList);
                } else {
                    for (TransactionInfo txInfo : baseList) {
                        if (txInfo.getTxType() == type) {
                            result.add(txInfo);
                        }
                    }
                }
            }
        }

        result.sort(new Comparator<TransactionInfo>() {
            @Override
            public int compare(TransactionInfo o1, TransactionInfo o2) {
                return o1.compareTo(o2.getTime());
            }
        });

        Page<TransactionInfoDto> page = new Page<>(pageNumber, pageSize, result.size());
        int start = pageNumber * pageSize - pageSize;
        if (start >= page.getTotal()) {
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        int end = start + pageSize;
        if (end > page.getTotal()) {
            end = (int) page.getTotal();
        }

        List<TransactionInfoDto> infoDtoList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            TransactionInfo info = result.get(i);
            Transaction tx = ledgerService.getTx(info.getTxHash());
            if (tx == null) {
                tx = accountLedgerService.getUnconfirmedTransaction(info.getTxHash()).getData();
            }
            if (tx == null) {
                continue;
            }
            info.setBlockHeight(tx.getBlockHeight());
            // 非合约Token转账
            if (info.getTxType() != 1000) {
                info.setInfo(tx.getInfo(addressBytes));
            }
            infoDtoList.add(new TransactionInfoDto(info));

        }
        page.setList(infoDtoList);

        dtoResult.setSuccess(true);
        dtoResult.setData(page);
        return dtoResult.toRpcClientResult();
    }

    @GET
    @Path("/utxo/lock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询用户冻结列表", notes = "result.data: balanceJson 返回账户相关的冻结UTXO列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getLockUtxo(@ApiParam(name = "address", value = "地址")
                                       @PathParam("address") String address,
                                       @ApiParam(name = "pageNumber", value = "页码")
                                       @QueryParam("pageNumber") Integer pageNumber,
                                       @ApiParam(name = "pageSize", value = "每页条数")
                                       @QueryParam("pageSize") Integer pageSize) {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        byte[] addressBytes = null;
        Result dtoResult = new Result<>();

        try {
            addressBytes = AddressTool.getAddress(address);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        //获取所有锁定的utxo
        Result<List<Coin>> result = accountLedgerService.getLockedUtxo(addressBytes);
        if (result.isFailed()) {
            dtoResult.setSuccess(false);
            dtoResult.setErrorCode(result.getErrorCode());
            return dtoResult.toRpcClientResult();
        }

        List<Coin> coinList = result.getData();
        Page<UtxoDto> page = new Page<>(pageNumber, pageSize, result.getData().size());
        int start = pageNumber * pageSize - pageSize;
        if (start >= coinList.size()) {
            dtoResult.setSuccess(true);
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        List<UtxoDto> utxoDtoList = new ArrayList<>();
        byte[] txHash = new byte[NulsDigestData.HASH_LENGTH];
        for (Coin coin : coinList) {
            //找到每一条uxto，对应的交易类型与时间
            System.arraycopy(coin.getOwner(), 0, txHash, 0, NulsDigestData.HASH_LENGTH);
            Transaction tx = ledgerService.getTx(txHash);
            if (tx == null) {
                NulsDigestData hash = new NulsDigestData();
                try {
                    hash.parse(txHash, 0);
                    tx = accountLedgerService.getUnconfirmedTransaction(hash).getData();
                } catch (NulsException e) {
                    Log.error(e);
                    return Result.getFailed(KernelErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
                }
            }
            //考虑到数据回滚，会出现找不到的情况
            if (tx == null) {
                continue;
            }
            utxoDtoList.add(new UtxoDto(coin, tx));
        }

        //重新赋值page对象
        page = new Page<>(pageNumber, pageSize, utxoDtoList.size());
        if (start >= page.getTotal()) {
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        Collections.sort(utxoDtoList, UtxoDtoComparator.getInstance());
        int end = start + pageSize;
        if (end > utxoDtoList.size()) {
            end = utxoDtoList.size();
        }

        page.setList(utxoDtoList.subList(start, end));
        dtoResult.setSuccess(true);
        dtoResult.setData(page);
        return dtoResult.toRpcClientResult();
    }

    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    @GET
    @Path("/tx/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据hash查询交易")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = TransactionDto.class)
    })
    public RpcClientResult getTxByHash(@ApiParam(name = "hash", value = "交易hash", required = true)
                                       @PathParam("hash") String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!NulsDigestData.validHash(hash)) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = getUnconfirmedTx(hash);
        if (result.isSuccess()) {
            return result.toRpcClientResult();
        }
        return getConfirmedTx(hash).toRpcClientResult();
    }

    /**
     * 获取未确认的交易
     */
    private Result getUnconfirmedTx(String hash) {
        Result result = null;
        try {
            Result<Transaction> txResult = accountLedgerService.getUnconfirmedTransaction(NulsDigestData.fromDigestHex(hash));
            if (txResult.isFailed() || null == txResult.getData()) {
                result = Result.getFailed(TransactionErrorCode.TX_NOT_EXIST);
            } else {
                Transaction tx = txResult.getData();
                tx.setStatus(TxStatusEnum.UNCONFIRM);
                TransactionDto txDto = null;
                CoinData coinData = tx.getCoinData();
                if (coinData != null) {
                    // 组装from数据
                    List<Coin> froms = coinData.getFrom();
                    if (froms != null && froms.size() > 0) {
                        byte[] fromHash, owner;
                        int fromIndex;
                        NulsDigestData fromHashObj;
                        Transaction fromTx;
                        Coin fromUtxo;
                        for (Coin from : froms) {
                            owner = from.getOwner();
                            // owner拆分出txHash和index
                            fromHash = AccountLegerUtils.getTxHashBytes(owner);
                            fromIndex = AccountLegerUtils.getIndex(owner);
                            // 查询from UTXO
                            fromHashObj = new NulsDigestData();
                            fromHashObj.parse(fromHash, 0);
                            //获取上一笔的to,先查未确认,如果没有再查已确认
                            fromTx = accountLedgerService.getUnconfirmedTransaction(fromHashObj).getData();
                            if (null == fromTx) {
                                fromTx = ledgerService.getTx(fromHashObj);
                            }
                            fromUtxo = fromTx.getCoinData().getTo().get(fromIndex);
                            from.setFrom(fromUtxo);
                        }
                    }
                    txDto = new TransactionDto(tx);
                    List<OutputDto> outputDtoList = new ArrayList<>();
                    // 组装to数据
                    List<Coin> tos = coinData.getTo();
                    if (tos != null && tos.size() > 0) {
                        String txHash = hash;
                        OutputDto outputDto = null;
                        Coin to;
                        for (int i = 0, length = tos.size(); i < length; i++) {
                            to = tos.get(i);
                            outputDto = new OutputDto(to);
                            outputDto.setTxHash(txHash);
                            outputDto.setIndex(i);
                            outputDto.setStatus(0);
                            outputDtoList.add(outputDto);
                        }
                    }
                    txDto.setOutputs(outputDtoList);
                    // 计算交易实际发生的金额
                    calTransactionValue(txDto);
                }
                result = Result.getSuccess();
                result.setData(txDto);
            }
        } catch (NulsRuntimeException re) {
            Log.error(re);
            result = Result.getFailed(re.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result;
    }

    /**
     * 获取已确认的交易
     */
    private Result getConfirmedTx(String hash) {
        Result result = null;
        try {
            Transaction tx = ledgerService.getTx(NulsDigestData.fromDigestHex(hash));
            if (tx == null) {
                result = Result.getFailed(TransactionErrorCode.TX_NOT_EXIST);
            } else {
                tx.setStatus(TxStatusEnum.CONFIRMED);
                TransactionDto txDto = null;
                CoinData coinData = tx.getCoinData();
                if (coinData != null) {
                    // 组装from数据
                    List<Coin> froms = coinData.getFrom();
                    if (froms != null && froms.size() > 0) {
                        byte[] fromHash, owner;
                        int fromIndex;
                        NulsDigestData fromHashObj;
                        Transaction fromTx;
                        Coin fromUtxo;
                        for (Coin from : froms) {
                            owner = from.getOwner();
                            // owner拆分出txHash和index
                            fromHash = AccountLegerUtils.getTxHashBytes(owner);
                            fromIndex = AccountLegerUtils.getIndex(owner);
                            // 查询from UTXO
                            fromHashObj = new NulsDigestData();
                            fromHashObj.parse(fromHash, 0);
                            fromTx = ledgerService.getTx(fromHashObj);
                            fromUtxo = fromTx.getCoinData().getTo().get(fromIndex);
                            from.setFrom(fromUtxo);
                        }
                    }
                    txDto = new TransactionDto(tx);
                    List<OutputDto> outputDtoList = new ArrayList<>();
                    // 组装to数据
                    List<Coin> tos = coinData.getTo();
                    if (tos != null && tos.size() > 0) {
                        byte[] txHashBytes = tx.getHash().serialize();
                        String txHash = hash;
                        OutputDto outputDto = null;
                        Coin to, temp;
                        long bestHeight = NulsContext.getInstance().getBestHeight();
                        long currentTime = TimeService.currentTimeMillis();
                        long lockTime;
                        for (int i = 0, length = tos.size(); i < length; i++) {
                            to = tos.get(i);
                            outputDto = new OutputDto(to);
                            outputDto.setTxHash(txHash);
                            outputDto.setIndex(i);
                            temp = ledgerService.getUtxo(Arrays.concatenate(txHashBytes, new VarInt(i).encode()));
                            if (temp == null) {
                                // 已花费
                                outputDto.setStatus(3);
                            } else {
                                lockTime = temp.getLockTime();
                                if (lockTime < 0) {
                                    // 共识锁定
                                    outputDto.setStatus(2);
                                } else if (lockTime == 0) {
                                    // 正常未花费
                                    outputDto.setStatus(0);
                                } else if (lockTime > NulsConstant.BlOCKHEIGHT_TIME_DIVIDE) {
                                    // 判定是否时间高度锁定
                                    if (lockTime > currentTime) {
                                        // 时间高度锁定
                                        outputDto.setStatus(1);
                                    } else {
                                        // 正常未花费
                                        outputDto.setStatus(0);
                                    }
                                } else {
                                    // 判定是否区块高度锁定
                                    if (lockTime > bestHeight) {
                                        // 区块高度锁定
                                        outputDto.setStatus(1);
                                    } else {
                                        // 正常未花费
                                        outputDto.setStatus(0);
                                    }
                                }
                            }
                            outputDtoList.add(outputDto);
                        }
                    }
                    txDto.setOutputs(outputDtoList);
                    // 计算交易实际发生的金额
                    calTransactionValue(txDto);
                }
                result = Result.getSuccess();
                result.setData(txDto);
            }
        } catch (NulsRuntimeException re) {
            Log.error(re);
            result = Result.getFailed(re.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result;
    }


    /**
     * 计算交易实际发生的金额(只用于转账)
     * Calculate the actual amount of the transaction.
     */
    private void calTransactionValue(TransactionDto txDto) {
        if (txDto == null) {
            return;
        }
        List<InputDto> inputDtoList = txDto.getInputs();
        Set<String> inputAdressSet = new HashSet<>(inputDtoList.size());
        for (InputDto inputDto : inputDtoList) {
            inputAdressSet.add(inputDto.getAddress());
        }
        Na value = Na.ZERO;
        List<OutputDto> outputDtoList = txDto.getOutputs();
        for (OutputDto outputDto : outputDtoList) {
            if (inputAdressSet.contains(outputDto.getAddress())) {
                continue;
            }
            value = value.add(Na.valueOf(outputDto.getValue()));
        }
        txDto.setValue(value.getValue());
    }


    @POST
    @Path("/transferP2sh")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "多签转账", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transferP2sh(@ApiParam(name = "form", value = "转账", required = true) MultiSignForm form) {
        List<MultipleAddressTransferModel> toModelList = new ArrayList<>();
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getSignAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Na value = Na.valueOf(form.getAmount());
        Result result = null;
        if(form.getTxdata() == null || form.getTxdata().trim().length() == 0) {
            if (!AddressTool.validAddress(form.getAddress())) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            if (form.getOutputs() == null || form.getOutputs() == null) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            if (form.getAmount() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (form.getM() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (form.getPubkeys() == null || form.getPubkeys().size() == 0 || form.getPubkeys().size() < form.getM()) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            for (MultipleTxToDto to : form.getOutputs()) {
                MultipleAddressTransferModel model = new MultipleAddressTransferModel();
                if (!AddressTool.validAddress(to.getToAddress())) {
                    return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
                }
                model.setAddress(AddressTool.getAddress(to.getToAddress()));
                model.setAmount(to.getAmount());
                toModelList.add(model);
            }
            result = accountLedgerService.transferP2sh(AddressTool.getAddress(form.getAddress()),
                    AddressTool.getAddress(form.getSignAddress()),
                    toModelList,value,form.getPassword(),form.getRemark(),
                    TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES,form.getPubkeys(),form.getM(),null
            );
        }else{
            result = accountLedgerService.transferP2sh(null,
                    AddressTool.getAddress(form.getSignAddress()),
                    toModelList,value,form.getPassword(),form.getRemark(),
                    TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES,form.getPubkeys(),form.getM(),form.getTxdata()
            );
        }
        if (result.isSuccess()) {
            Map<String, String> map = new HashMap<>();
            map.put("value", (String) result.getData());
            result.setData(map);
        }
        return result.toRpcClientResult();
    }
}
