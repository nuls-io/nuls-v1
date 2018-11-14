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
package io.nuls.contract.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.entity.ContractInfoDto;
import io.nuls.contract.entity.tx.CreateContractTransaction;
import io.nuls.contract.entity.txdata.ContractData;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.contract.helper.VMHelper;
import io.nuls.contract.ledger.manager.ContractBalanceManager;
import io.nuls.contract.ledger.module.ContractBalance;
import io.nuls.contract.ledger.service.ContractTransactionInfoService;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.ledger.util.ContractLedgerUtil;
import io.nuls.contract.rpc.form.*;
import io.nuls.contract.rpc.model.*;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.po.ContractCollectionInfoPo;
import io.nuls.contract.storage.po.TransactionInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractCollectionStorageService;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.contract.storage.service.ContractUtxoStorageService;
import io.nuls.contract.util.ContractCoinComparator;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.*;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.model.Entry;
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
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.util.LedgerUtil;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.nuls.contract.constant.ContractConstant.MAX_GASLIMIT;
import static io.nuls.contract.util.ContractUtil.checkVmResultAndReturn;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 */
@Path("/contract")
@Api(value = "/contract", description = "contract")
@Component
public class ContractResource implements InitializingBean {

    @Autowired
    private ContractTxService contractTxService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractUtxoStorageService contractUtxoStorageService;

    @Autowired
    private ContractTransactionInfoService contractTransactionInfoService;

    @Autowired
    private ContractCollectionStorageService contractCollectionStorageService;

    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    @Autowired
    private ContractUtxoService contractUtxoService;

    @Autowired
    private ContractBalanceManager contractBalanceManager;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private VMHelper vmHelper;

    @Autowired
    private VMContext vmContext;

    private ProgramExecutor programExecutor;

    @Override
    public void afterPropertiesSet() throws NulsException {
        programExecutor = vmHelper.getProgramExecutor();
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "创建智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult createContract(@ApiParam(name = "createForm", value = "创建智能合约", required = true) ContractCreate create) {
        if (create == null || create.getGasLimit() < 0 || create.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(create.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractCode = create.getContractCode();
        if(StringUtils.isBlank(contractCode)) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        byte[] contractCodeBytes = Hex.decode(contractCode);

        ProgramMethod method = vmHelper.getMethodInfoByCode(ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
        String[][] args = null;
        if(method != null) {
            args = create.getArgs(method.argsType2Array());
        }

        return contractTxService.contractCreateTx(create.getSender(),
                create.getGasLimit(),
                create.getPrice(),
                contractCodeBytes,
                args,
                create.getPassword(),
                create.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/validate/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证创建智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult validateCreateContract(@ApiParam(name = "validateCreateForm", value = "验证创建智能合约", required = true) ContractValidateCreate create) {
        if (create == null || create.getGasLimit() < 0 || create.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        String contractCode = create.getContractCode();
        if(StringUtils.isBlank(contractCode)) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        byte[] contractCodeBytes = Hex.decode(contractCode);

        ProgramMethod method = vmHelper.getMethodInfoByCode(ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
        String[][] args = null;
        if(method != null) {
            args = create.getArgs(method.argsType2Array());
        }

        return contractTxService.validateContractCreateTx(
                create.getGasLimit(),
                create.getPrice(),
                contractCodeBytes,
                args).toRpcClientResult();
    }

    @POST
    @Path("/constructor")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约构造函数")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractInfoDto.class)
    })
    public RpcClientResult contractConstructor(@ApiParam(name = "createForm", value = "创建智能合约", required = true) ContractCode code) {
        if (code == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        String contractCode = code.getContractCode();
        if(StringUtils.isBlank(contractCode)) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        byte[] contractCodeBytes = Hex.decode(contractCode);
        ContractInfoDto contractInfoDto = vmHelper.getConstructor(contractCodeBytes);
        if(contractInfoDto == null || contractInfoDto.getConstructor() == null) {
            return Result.getFailed(ContractErrorCode.ILLEGAL_CONTRACT).toRpcClientResult();
        }
        Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
        resultMap.put("constructor", contractInfoDto.getConstructor());
        resultMap.put("isNrc20", contractInfoDto.isNrc20());
        return Result.getSuccess().setData(resultMap).toRpcClientResult();
    }

    @POST
    @Path("/precreate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试创建智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult preCreateContract(@ApiParam(name = "preCreateForm", value = "测试创建智能合约", required = true) PreContractCreate create) {
        if (create == null || create.getGasLimit() < 0 || create.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(create.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractCode = create.getContractCode();
        if(StringUtils.isBlank(contractCode)) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        byte[] contractCodeBytes = Hex.decode(contractCode);

        ProgramMethod method = vmHelper.getMethodInfoByCode(ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
        String[][] args = null;
        if(method != null) {
            args = create.getArgs(method.argsType2Array());
        }

        return contractTxService.contractPreCreateTx(create.getSender(),
                create.getGasLimit(),
                create.getPrice(),
                contractCodeBytes,
                args,
                null,
                create.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/imputedgas/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "估算创建智能合约的Gas消耗")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult imputedGasCreateContract(@ApiParam(name = "imputedGasCreateForm", value = "估算创建智能合约的Gas消耗", required = true) ImputedGasContractCreate create) {
        try {
            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);
            long price = create.getPrice();
            if (create == null || price <= 0) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            String sender = create.getSender();
            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            String contractCode = create.getContractCode();
            if(StringUtils.isBlank(contractCode)) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            // 生成一个地址作为智能合约地址
            Address contractAddress = AccountTool.createContractAddress();
            byte[] contractAddressBytes = contractAddress.getAddressBytes();
            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractCodeBytes = Hex.decode(contractCode);

            ProgramMethod method = vmHelper.getMethodInfoByCode(ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] args = null;
            if(method != null) {
                args = create.getArgs(method.argsType2Array());
            }

            // 当前区块高度
            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");
            // 执行VM估算Gas消耗
            ProgramCreate programCreate = new ProgramCreate();
            programCreate.setContractAddress(contractAddressBytes);
            programCreate.setSender(senderBytes);
            programCreate.setValue(BigInteger.valueOf(0L));
            programCreate.setPrice(price);
            programCreate.setGasLimit(MAX_GASLIMIT);
            programCreate.setNumber(blockHeight);
            programCreate.setContractCode(contractCodeBytes);
            if(args != null) {
                programCreate.setArgs(args);
            }
            programCreate.setEstimateGas(true);

            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.create(programCreate);
            if(!programResult.isSuccess()) {
                Log.error(programResult.getStackTrace());
                return checkVmResultAndReturn(programResult.getErrorMessage(), Result.getSuccess().setData(resultMap)).toRpcClientResult();
            }
            long gasUsed = programResult.getGasUsed();
            // 预估1.5倍Gas
            gasUsed += gasUsed >> 1;
            gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
            resultMap.put("gasLimit", gasUsed);
            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (Exception e) {
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    @POST
    @Path("/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "调用智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult callContract(@ApiParam(name = "callForm", value = "调用智能合约", required = true) ContractCall call) {
        if (call == null || call.getValue() < 0 || call.getGasLimit() < 0 || call.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(call.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = call.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        ProgramMethod method = vmHelper.getMethodInfoByContractAddress(call.getMethodName(), call.getMethodDesc(), contractAddressBytes);
        String[][] args = null;
        if(method != null) {
            args = call.getArgs(method.argsType2Array());
        }

        return contractTxService.contractCallTx(call.getSender(),
                Na.valueOf(call.getValue()),
                call.getGasLimit(),
                call.getPrice(),
                contractAddress,
                call.getMethodName(),
                call.getMethodDesc(),
                args,
                call.getPassword(),
                call.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/validate/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证调用智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult validateCallContract(@ApiParam(name = "validateCallForm", value = "验证调用智能合约", required = true) ContractValidateCall call) {
        if (call == null || call.getGasLimit() < 0 || call.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(call.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = call.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        ProgramMethod method = vmHelper.getMethodInfoByContractAddress(call.getMethodName(), call.getMethodDesc(), contractAddressBytes);
        String[][] args = null;
        if(method != null) {
            args = call.getArgs(method.argsType2Array());
        }

        return contractTxService.validateContractCallTx(call.getSender(),
                call.getGasLimit(),
                call.getPrice(),
                contractAddress,
                call.getMethodName(),
                call.getMethodDesc(),
                args).toRpcClientResult();
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "向智能合约地址转账")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transfer(@ApiParam(name = "transferForm", value = "向合约地址转账", required = true) ContractTransfer transfer) {
        if (transfer == null || transfer.getAmount() < 0 || transfer.getGasLimit() < 0 || transfer.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(transfer.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = transfer.getToAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        return contractTxService.contractCallTx(transfer.getAddress(),
                Na.valueOf(transfer.getAmount()),
                transfer.getGasLimit(),
                transfer.getPrice(),
                contractAddress,
                ContractConstant.BALANCE_TRIGGER_METHOD_NAME,
                ContractConstant.BALANCE_TRIGGER_METHOD_DESC,
                null,
                transfer.getPassword(),
                transfer.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/token/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "token转账")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult tokenTransfer(@ApiParam(name = "tokenTransferForm", value = "token转账", required = true) ContractTokenTransfer transfer) {
        if (transfer == null || transfer.getAmount() == null ||
                !StringUtils.isNumeric(transfer.getAmount()) ||
                new BigInteger(transfer.getAmount()).compareTo(BigInteger.ZERO) < 0 || transfer.getGasLimit() < 0 || transfer.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        String from = transfer.getAddress();
        String to = transfer.getToAddress();
        if (!AddressTool.validAddress(from)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(to)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = transfer.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        Result<ContractAddressInfoPo> contractAddressInfoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
        ContractAddressInfoPo po = contractAddressInfoResult.getData();
        if(po == null) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }
        if(!po.isNrc20()) {
            return Result.getFailed(ContractErrorCode.CONTRACT_NOT_NRC20).toRpcClientResult();
        }
        Object[] argsObj = new Object[] {to, transfer.getAmount()};

        return contractTxService.contractCallTx(transfer.getAddress(),
                Na.ZERO,
                transfer.getGasLimit(),
                transfer.getPrice(),
                contractAddress,
                ContractConstant.NRC20_METHOD_TRANSFER,
                null,
                ContractUtil.twoDimensionalArray(argsObj),
                transfer.getPassword(),
                transfer.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/transfer/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "向智能合约地址转账手续费")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transferFee(@ApiParam(name = "transferFeeForm", value = "向合约地址转账手续费", required = true) ContractTransferFee transferFee) {
        if (transferFee == null || transferFee.getAmount() < 0 || transferFee.getGasLimit() < 0 || transferFee.getPrice() < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        String fromAddress = transferFee.getAddress();
        if (!AddressTool.validAddress(fromAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = transferFee.getToAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        Result result = contractTxService.transferFee(fromAddress,
                Na.valueOf(transferFee.getAmount()),
                transferFee.getGasLimit(),
                transferFee.getPrice(),
                contractAddress,
                ContractConstant.BALANCE_TRIGGER_METHOD_NAME,
                ContractConstant.BALANCE_TRIGGER_METHOD_DESC,
                null,
                transferFee.getRemark());
        if(result.isSuccess()) {
            Object[] datas = (Object[]) result.getData();
            if(datas == null) {
                return Result.getFailed(ContractErrorCode.DATA_ERROR).toRpcClientResult();
            }
            Na fee = (Na) datas[0];
            Transaction tx = (Transaction) datas[1];
            Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(fromAddress), tx,
                    TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            Map<String, Long> map = new HashMap<>();
            Long maxAmount = null;
            if (rs.isSuccess()) {
                maxAmount = ((Na) rs.getData()).getValue();
            }
            map.put("fee", fee.getValue());
            map.put("maxAmount", maxAmount);
            result.setData(map);
            return result.toRpcClientResult();
        } else {
            return result.toRpcClientResult();
        }
    }

    @POST
    @Path("/view")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "调用不上链的智能合约函数")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult invokeViewContract(
            @ApiParam(name = "constantCallForm", value = "调用不上链的智能合约函数表单数据", required = true) ContractViewCall viewCall) {
        try {
            String contractAddress = viewCall.getContractAddress();
            String methodName = viewCall.getMethodName();
            if (StringUtils.isBlank(contractAddress) || StringUtils.isBlank(methodName)) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
            }

            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
            }

            ProgramMethod method = vmHelper.getMethodInfoByContractAddress(methodName, viewCall.getMethodDesc(), contractAddressBytes);

            if(method == null || !method.isView()) {
                return Result.getFailed(ContractErrorCode.CONTRACT_NON_VIEW_METHOD).toRpcClientResult();
            }

            ProgramResult programResult = vmHelper.invokeViewMethod(contractAddressBytes, methodName, viewCall.getMethodDesc(),
                    viewCall.getArgs(method.argsType2Array()));

            Result result;
            if(!programResult.isSuccess()) {
                Log.error(programResult.getStackTrace());
                result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
            } else {
                result = Result.getSuccess();
                Map<String, String> resultMap = MapUtil.createLinkedHashMap(2);
                resultMap.put("result", programResult.getResult());
                result.setData(resultMap);
            }
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error("invoke contract view method error.", e);
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    @POST
    @Path("/imputedgas/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "估算调用智能合约的Gas消耗")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult imputedGasCallContract(@ApiParam(name = "imputedGasCallForm", value = "估算调用智能合约的Gas消耗", required = true) ImputedGasContractCall call) {
        try {

            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);

            String sender = call.getSender();
            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            String contractAddress = call.getContractAddress();
            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            String methodName = call.getMethodName();
            // 如果是不上链的方法，不消耗gas，直接返回0
            ProgramMethod programMethod = vmHelper.getMethodInfoByContractAddress(methodName, call.getMethodDesc(), contractAddressBytes);
            if(programMethod == null || programMethod.isView()) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            // 当前区块高度
            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");


            long price = call.getPrice();
            if (call == null || call.getValue() < 0 || call.getPrice() <= 0) {
                return Result.getSuccess().setData(resultMap).toRpcClientResult();
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            String[][] args = null;
            if(programMethod != null) {
                args = call.getArgs(programMethod.argsType2Array());
            }

            // 执行VM估算Gas消耗
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddressBytes);
            programCall.setSender(senderBytes);
            programCall.setValue(BigInteger.valueOf(call.getValue()));
            programCall.setPrice(price);
            programCall.setGasLimit(MAX_GASLIMIT);
            programCall.setNumber(blockHeight);
            programCall.setMethodName(call.getMethodName());
            programCall.setMethodDesc(call.getMethodDesc());
            programCall.setArgs(args);
            programCall.setEstimateGas(true);

            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.call(programCall);
            if(!programResult.isSuccess()) {
                Log.error(programResult.getStackTrace());
                return checkVmResultAndReturn(programResult.getErrorMessage(), Result.getSuccess().setData(resultMap)).toRpcClientResult();
            }
            long gasUsed = programResult.getGasUsed();
            // 预估1.5倍Gas
            gasUsed += gasUsed >> 1;
            gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
            resultMap.put("gasLimit", gasUsed);
            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    @POST
    @Path("/imputedprice")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "估算智能合约的price")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult imputedPrice(@ApiParam(name = "imputedPriceForm", value = "估算智能合约的price", required = true) ImputedPrice imputedPrice) {
        try {

            String address = imputedPrice.getSender();
            if (!AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }

            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            byte[] addressBytes = AddressTool.getAddress(address);
            long price = vmHelper.getLastedPriceForAccount(addressBytes);

            return Result.getSuccess().setData(price).toRpcClientResult();
        } catch (Exception e) {
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "删除智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult deleteContract(@ApiParam(name = "deleteForm", value = "删除智能合约", required = true) ContractDelete delete) {
        if (delete == null) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(delete.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = delete.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        return contractTxService.contractDeleteTx(delete.getSender(),
                contractAddress,
                delete.getPassword(),
                delete.getRemark()).toRpcClientResult();
    }

    @POST
    @Path("/validate/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证删除智能合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult validateDeleteContract(@ApiParam(name = "validateDeleteForm", value = "验证删除智能合约", required = true) ContractValidateDelete delete) {
        if (delete == null) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(delete.getSender())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        String contractAddress = delete.getContractAddress();
        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        return contractTxService.validateContractDeleteTx(delete.getSender(),
                contractAddress).toRpcClientResult();
    }


    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证是否为合约地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult validateContractAddress(@ApiParam(name="address", value="地址", required = true)
                                                   @PathParam("address") String address) {
        if (StringUtils.isBlank(address)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        try {
            boolean isContractAddress = false;
            boolean isPayable = false;
            boolean isNrc20 = false;
            long decimals = 0L;
            do {
                byte[] contractAddressBytes = AddressTool.getAddress(address);
                Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
                if(contractAddressInfoPoResult.isFailed()) {
                    break;
                }
                ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
                if(contractAddressInfoPo == null) {
                    break;
                }
                isContractAddress = true;
                isPayable = contractAddressInfoPo.isAcceptDirectTransfer();
                isNrc20 = contractAddressInfoPo.isNrc20();
                if(isNrc20) {
                    decimals = contractAddressInfoPo.getDecimals();
                }
            } while (false);
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("isContractAddress", isContractAddress);
            resultMap.put("isPayable", isPayable);
            resultMap.put("isNrc20", isNrc20);
            if(isNrc20) {
                resultMap.put("decimals", decimals);
            }


            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (Exception e) {
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    @GET
    @Path("/info/wallet/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult getContractInfo(
            @ApiParam(name = "address", value = "合约地址", required = true) @PathParam("address") String contractAddress,
            @ApiParam(name = "accountAddress", value = "钱包账户地址", required = false) @QueryParam("accountAddress") String accountAddress) {
        return this.getContractInfoWithLock(contractAddress, accountAddress, true);
    }

    @GET
    @Path("/info/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult getContractInfo(
            @ApiParam(name = "address", value = "合约地址", required = true) @PathParam("address") String contractAddress) {
        return this.getContractInfoWithLock(contractAddress, null, false);

    }

    private RpcClientResult getContractInfoWithLock(
            String contractAddress,
            String accountAddress,
            boolean isNeedLock) {
        try {
            boolean hasAccountAddress = false;
            if(StringUtils.isNotBlank(accountAddress)) {
                Result<Account> accountResult = accountService.getAccount(accountAddress);
                if (accountResult.isFailed()) {
                    return accountResult.toRpcClientResult();
                }
                hasAccountAddress = true;
            }

            if (contractAddress == null) {
                return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
            if(contractAddressInfoPoResult.isFailed()) {
                return contractAddressInfoPoResult.toRpcClientResult();
            }
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            if(contractAddressInfoPo == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
            }

            if(isNeedLock && contractAddressInfoPo.isLock()) {
                return Result.getFailed(ContractErrorCode.CONTRACT_LOCK).toRpcClientResult();
            }

            byte[] prevStateRoot = ContractUtil.getStateRoot(NulsContext.getInstance().getBestBlock().getHeader());

            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramStatus status = track.status(contractAddressBytes);
            List<ProgramMethod> methods = track.method(contractAddressBytes);

            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(8);
            try {
                byte[] createTxHash = contractAddressInfoPo.getCreateTxHash();
                NulsDigestData create = new NulsDigestData();
                create.parse(createTxHash, 0);
                resultMap.put("createTxHash", create.getDigestHex());
            } catch (Exception e) {
                Log.error("createTxHash parse error.", e);
            }

            if(hasAccountAddress) {
                // 所有收藏的合约列表
                boolean isCollect = false;
                Result<ContractCollectionInfoPo> collectionInfoPoResult = contractCollectionStorageService.getContractAddress(contractAddressBytes);
                ContractCollectionInfoPo contractCollectionPo = collectionInfoPoResult.getData();
                if (contractCollectionPo != null) {
                    if(contractCollectionPo.getCollectorMap().containsKey(accountAddress)) {
                        isCollect = true;
                    }
                }
                resultMap.put("isCollect", isCollect);
            }
            resultMap.put("address", contractAddress);
            resultMap.put("creater", AddressTool.getStringAddressByBytes(contractAddressInfoPo.getSender()));
            resultMap.put("createTime", contractAddressInfoPo.getCreateTime());
            resultMap.put("blockHeight", contractAddressInfoPo.getBlockHeight());
            resultMap.put("isNrc20", contractAddressInfoPo.isNrc20());
            if(contractAddressInfoPo.isNrc20()) {
                resultMap.put("nrc20TokenName", contractAddressInfoPo.getNrc20TokenName());
                resultMap.put("nrc20TokenSymbol", contractAddressInfoPo.getNrc20TokenSymbol());
                resultMap.put("decimals", contractAddressInfoPo.getDecimals());
                resultMap.put("totalSupply", ContractUtil.bigInteger2String(contractAddressInfoPo.getTotalSupply()));
            }
            resultMap.put("status", status.name());
            resultMap.put("method", methods);

            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }

    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约余额")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult getContractBalance(@ApiParam(name = "address", value = "合约地址", required = true) @PathParam("address") String contractAddress) {
        if (contractAddress == null) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        if (!AddressTool.validAddress(contractAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        Result<ContractBalance> result = contractUtxoService.getBalance(contractAddressBytes);
        ContractBalance balance = (ContractBalance) result.getData();
        Map<String, Object> resultMap = MapUtil.createLinkedHashMap(4);
        resultMap.put("address", contractAddress);
        resultMap.put("balance", balance == null ? Na.ZERO : balance.getBalance().toString());
        resultMap.put("usable", balance == null ? Na.ZERO : balance.getRealUsable().toString());
        resultMap.put("locked", balance == null ? Na.ZERO : balance.getLocked().toString());
        return Result.getSuccess().setData(resultMap).toRpcClientResult();
    }

    @GET
    @Path("/balance/token/{contractAddress}/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取账户地址的指定token余额")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult getAccountTokenBalance(
            @ApiParam(name = "contractAddress", value = "合约地址", required = true) @PathParam("contractAddress") String contractAddress,
            @ApiParam(name = "address", value = "账户地址", required = true) @PathParam("address") String address) {
        Result<ContractTokenInfo> tokenInfoResult = contractService.getContractTokenViaVm(address, contractAddress);
        if(tokenInfoResult.isFailed()) {
            return tokenInfoResult.toRpcClientResult();
        }
        ContractTokenInfo data = tokenInfoResult.getData();
        ContractTokenInfoDto dto = null;
        if(data != null) {
            dto = new ContractTokenInfoDto(data);
            dto.setStatus(data.getStatus());
        }
        return Result.getSuccess().setData(dto).toRpcClientResult();
    }

    @GET
    @Path("/result/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约执行结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractResultDto.class)
    })
    public RpcClientResult getContractTxResult(@ApiParam(name="hash", value="交易hash", required = true)
                                          @PathParam("hash") String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!NulsDigestData.validHash(hash)) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        try {
            ContractResultDto contractResultDto = null;
            ContractResult contractExecuteResult;
            boolean flag = true;
            String msg = EMPTY;
            //long confirmCount = 0L;
            do {
                NulsDigestData txHash = NulsDigestData.fromDigestHex(hash);
                Transaction tx = ledgerService.getTx(txHash);
                if (tx == null) {
                    flag = false;
                    msg = TransactionErrorCode.TX_NOT_EXIST.getMsg();
                    break;
                } else {
                    if (!ContractUtil.isContractTransaction(tx)) {
                        flag = false;
                        msg = ContractErrorCode.NON_CONTRACTUAL_TRANSACTION.getMsg();
                        break;
                    }
                }
                contractExecuteResult = contractService.getContractExecuteResult(txHash);
                if(contractExecuteResult != null) {
                    //long bestBlockHeight = NulsContext.getInstance().getBestHeight();
                    //confirmCount = bestBlockHeight - tx.getBlockHeight() + 1;
                    Result<ContractAddressInfoPo> contractAddressInfoResult =
                            contractAddressStorageService.getContractAddressInfo(contractExecuteResult.getContractAddress());
                    ContractAddressInfoPo po = contractAddressInfoResult.getData();
                    if(po != null && po.isNrc20()) {
                        contractExecuteResult.setNrc20(true);
                        if(contractExecuteResult.isSuccess()) {
                            contractResultDto = new ContractResultDto(contractExecuteResult, tx, po);
                        } else {
                            ContractData contractData = (ContractData) tx.getTxData();
                            byte[] sender = contractData.getSender();
                            byte[] infoKey = ArraysTool.concatenate(sender, tx.getHash().serialize(), new VarInt(0).encode());
                            Result<ContractTokenTransferInfoPo> tokenTransferResult = contractTokenTransferStorageService.getTokenTransferInfo(infoKey);
                            ContractTokenTransferInfoPo transferInfoPo = tokenTransferResult.getData();
                            contractResultDto = new ContractResultDto(contractExecuteResult, tx, po, transferInfoPo);
                        }
                    } else {
                        contractResultDto = new ContractResultDto(contractExecuteResult, tx);
                    }
                    break;
                } else {
                    flag = false;
                    msg = TransactionErrorCode.DATA_NOT_FOUND.getMsg();
                    break;
                }
            } while (false);
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("flag", flag);
            if(!flag && StringUtils.isNotBlank(msg)) {
                resultMap.put("msg", msg);
            }
            if(flag && contractResultDto != null) {
                List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
                List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(tokenTransfers);
                contractResultDto.setTokenTransfers(realTokenTransfers);
                resultMap.put("data", contractResultDto);
            }
            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }
    }

    private List<ContractTokenTransferDto> filterRealTokenTransfers(List<ContractTokenTransferDto> tokenTransfers) {
        if(tokenTransfers == null || tokenTransfers.isEmpty()) {
            return tokenTransfers;
        }
        List<ContractTokenTransferDto> resultDto = new ArrayList<>();
        Map<String, ContractAddressInfoPo> cache = MapUtil.createHashMap(tokenTransfers.size());
        for(ContractTokenTransferDto tokenTransfer : tokenTransfers) {
            try {
                if(StringUtils.isBlank(tokenTransfer.getName())) {
                    String contractAddress = tokenTransfer.getContractAddress();
                    ContractAddressInfoPo po = cache.get(contractAddress);
                    if(po == null) {
                        po = contractAddressStorageService.getContractAddressInfo(
                                AddressTool.getAddress(contractAddress)).getData();
                        cache.put(contractAddress, po);
                    }
                    if(po == null || !po.isNrc20()) {
                        continue;
                    }
                    tokenTransfer.setNrc20Info(po);
                    resultDto.add(tokenTransfer);
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return resultDto;
    }

    @GET
    @Path("/tx/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约交易详情")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractTransactionDto.class)
    })
    public RpcClientResult getContractTx(@ApiParam(name="hash", value="交易hash", required = true)
                                          @PathParam("hash") String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        if (!NulsDigestData.validHash(hash)) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Result result;
        try {
            NulsDigestData txHashObj = NulsDigestData.fromDigestHex(hash);
            Transaction tx = ledgerService.getTx(txHashObj);
            if (tx == null) {
                result = Result.getFailed(TransactionErrorCode.TX_NOT_EXIST);
            } else {
                if(!ContractUtil.isContractTransaction(tx) && tx.getType() != NulsConstant.TX_TYPE_COINBASE) {
                    return Result.getFailed(ContractErrorCode.NON_CONTRACTUAL_TRANSACTION).toRpcClientResult();
                }

                tx.setStatus(TxStatusEnum.CONFIRMED);
                ContractTransactionDto txDto = null;
                CoinData coinData = tx.getCoinData();
                byte[] txHashBytes = tx.getHash().serialize();
                if(coinData != null) {
                    // 组装from数据
                    List<Coin> froms = coinData.getFrom();
                    if(froms != null && froms.size() > 0) {
                        byte[] fromHash, owner;
                        int fromIndex;
                        NulsDigestData fromHashObj;
                        Transaction fromTx;
                        Coin fromUtxo;
                        for(Coin from : froms) {
                            owner = from.getOwner();
                            // owner拆分出txHash和index
                            fromHash = LedgerUtil.getTxHashBytes(owner);
                            fromIndex = LedgerUtil.getIndex(owner);
                            // 查询from UTXO
                            fromHashObj = new NulsDigestData();
                            fromHashObj.parse(fromHash,0);
                            fromTx = ledgerService.getTx(fromHashObj);
                            fromUtxo = fromTx.getCoinData().getTo().get(fromIndex);
                            from.setFrom(fromUtxo);
                        }
                    }
                    txDto = new ContractTransactionDto(tx);
                    List<OutputDto> outputDtoList = new ArrayList<>();
                    // 组装to数据
                    List<Coin> tos = coinData.getTo();
                    if(tos != null && tos.size() > 0) {
                        String txHash = hash;
                        OutputDto outputDto;
                        Coin to, temp;
                        long bestHeight = NulsContext.getInstance().getBestHeight();
                        long currentTime = TimeService.currentTimeMillis();
                        long lockTime;
                        for(int i = 0, length = tos.size(); i < length; i++) {
                            to = tos.get(i);
                            outputDto = new OutputDto(to);
                            outputDto.setTxHash(txHash);
                            outputDto.setIndex(i);
                            temp = ledgerService.getUtxo(org.spongycastle.util.Arrays.concatenate(txHashBytes, new VarInt(i).encode()));
                            if(temp == null) {
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
                // 获取合约执行结果
                if(tx.getType() != ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
                    ContractResult contractExecuteResult = contractService.getContractExecuteResult(txHashObj);
                    if(contractExecuteResult != null) {
                        Result<ContractAddressInfoPo> contractAddressInfoResult =
                                contractAddressStorageService.getContractAddressInfo(contractExecuteResult.getContractAddress());
                        ContractAddressInfoPo po = contractAddressInfoResult.getData();
                        if(po != null && po.isNrc20()) {
                            contractExecuteResult.setNrc20(true);
                            if(contractExecuteResult.isSuccess()) {
                                txDto.setContractResult(new ContractResultDto(contractExecuteResult, tx, po));
                            } else {
                                ContractData contractData = (ContractData) tx.getTxData();
                                byte[] sender = contractData.getSender();
                                byte[] infoKey = ArraysTool.concatenate(sender, txHashBytes, new VarInt(0).encode());
                                Result<ContractTokenTransferInfoPo> tokenTransferResult = contractTokenTransferStorageService.getTokenTransferInfo(infoKey);
                                ContractTokenTransferInfoPo transferInfoPo = tokenTransferResult.getData();
                                txDto.setContractResult(new ContractResultDto(contractExecuteResult, tx, po, transferInfoPo));
                            }
                        } else {
                            txDto.setContractResult(new ContractResultDto(contractExecuteResult, tx));
                        }
                        ContractResultDto contractResultDto = txDto.getContractResult();
                        List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
                        List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(tokenTransfers);
                        contractResultDto.setTokenTransfers(realTokenTransfers);
                    }
                }
                result = Result.getSuccess();
                result.setData(txDto);
            }
        } catch (NulsRuntimeException e) {
            Log.error(e);
            result = Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result.toRpcClientResult();
    }

    /**
     * 计算交易实际发生的金额
     * Calculate the actual amount of the transaction.
     *
     * @param txDto
     */
    private void calTransactionValue(ContractTransactionDto txDto) {
        if(txDto == null) {
            return;
        }
        List<InputDto> inputDtoList = txDto.getInputs();
        Set<String> inputAdressSet = new HashSet<>(inputDtoList.size());
        for(InputDto inputDto : inputDtoList) {
            inputAdressSet.add(inputDto.getAddress());
        }
        Na value = Na.ZERO;
        List<OutputDto> outputDtoList = txDto.getOutputs();
        for(OutputDto outputDto : outputDtoList) {
            if(inputAdressSet.contains(outputDto.getAddress())) {
                continue;
            }
            value = value.add(Na.valueOf(outputDto.getValue()));
        }
        txDto.setValue(value.getValue());
    }


    @GET
    @Path("/limit/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据address和limit查询合约UTXO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractAccountUtxoDto.class)
    })
    public RpcClientResult getUtxoByAddressAndLimit(
            @ApiParam(name="address", value="地址", required = true) @PathParam("address") String address,
            @ApiParam(name="limit", value="数量(不填查所有)", required = false) @QueryParam("limit") Integer limit) {
        if (StringUtils.isBlank(address)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(address);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        Result result;
        try {
            boolean isLoadAll = (limit == null);
            List<Coin> coinList = getAllUtxoByAddress(address);
            int limitValue = 0;
            if(!isLoadAll) {
                limitValue = limit.intValue();
            }
            ContractAccountUtxoDto accountUtxoDto = new ContractAccountUtxoDto();
            List<ContractUtxoDto> list = new LinkedList<>();
            int i = 0;
            for (Coin coin : coinList) {
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                if(!isLoadAll) {
                    if(i >= limitValue) {
                        break;
                    }
                    i++;
                }
                list.add(new ContractUtxoDto(coin));
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
    @ApiOperation(value = "根据address和amount查询合约UTXO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractAccountUtxoDto.class)
    })
    public RpcClientResult getUtxoByAddressAndAmount(
            @ApiParam(name="address", value="地址", required = true) @PathParam("address") String address,
            @ApiParam(name="amount", value="金额", required = true) @PathParam("amount") Long amount) {
        if (StringUtils.isBlank(address) || amount == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
        }

        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }

        byte[] contractAddressBytes = AddressTool.getAddress(address);
        if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
        }

        Result result;
        try {
            List<Coin> coinList = getAllUtxoByAddress(address);
            Na amountNa = Na.valueOf(amount.longValue());

            ContractAccountUtxoDto accountUtxoDto = new ContractAccountUtxoDto();
            List<ContractUtxoDto> list = new LinkedList<>();
            Na values = Na.ZERO;
            for (Coin coin : coinList) {
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                list.add(new ContractUtxoDto(coin));
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
        List<Entry<byte[], byte[]>> coinBytesList = contractUtxoStorageService.loadAllCoinList();
        Coin coin;
        for (Entry<byte[], byte[]> coinEntryBytes : coinBytesList) {
            coin = new Coin();
            try {
                coin.parse(coinEntryBytes.getValue(), 0);
            } catch (NulsException e) {
                Log.info("parse coin form db error");
                continue;
            }
            if (Arrays.equals(coin.getAddress(), addressBytes)) {
                coin.setOwner(coinEntryBytes.getKey());
                coinList.add(coin);
            }
        }
        Collections.sort(coinList, ContractCoinComparator.getInstance());
        return coinList;
    }

    @GET
    @Path("/tx/list/{contractAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取智能合约的交易列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractTransactionInfoDto.class)
    })
    public RpcClientResult getTxList(
            @ApiParam(name="contractAddress", value="智能合约地址", required = true)
            @PathParam("contractAddress") String contractAddress,
            @ApiParam(name = "pageNumber", value = "页码", required = true)
            @QueryParam("pageNumber") Integer pageNumber,
            @ApiParam(name = "pageSize", value = "每页条数", required = false)
            @QueryParam("pageSize") Integer pageSize,
            @ApiParam(name = "accountAddress", value = "钱包账户地址")
            @QueryParam("accountAddress") String accountAddress) {
        try {
            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            if (StringUtils.isBlank(contractAddress)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }

            boolean isFilterAccountAddress = false;
            if(StringUtils.isNotBlank(accountAddress)) {
                Result<Account> accountResult = accountService.getAccount(accountAddress);
                if (accountResult.isFailed()) {
                    return accountResult.toRpcClientResult();
                }
                isFilterAccountAddress = true;
            }

            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }


            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
            }

            Result<List<TransactionInfoPo>> txInfoPoListResult = contractTransactionInfoService.getTxInfoList(contractAddressBytes);
            List<TransactionInfoPo> orginTxInfoPoList = txInfoPoListResult.getData();
            List<TransactionInfoPo> txInfoPoList = new ArrayList<>();
            do {
                if(orginTxInfoPoList == null || orginTxInfoPoList.size() == 0) {
                    break;
                }

                Stream<TransactionInfoPo> transactionInfoPoStream = orginTxInfoPoList.stream()
                        .filter(po -> po.getTxType() != ContractConstant.TX_TYPE_CONTRACT_TRANSFER);
                // 筛选出和账户相关的合约交易
                if(isFilterAccountAddress) {
                    byte[] accountAddressBytes = AddressTool.getAddress(accountAddress);
                    txInfoPoList = transactionInfoPoStream.filter(po -> checkEquals(po.getAddresses(), accountAddressBytes, 0)).collect(Collectors.toList());
                } else {
                    txInfoPoList = transactionInfoPoStream.collect(Collectors.toList());;
                }
            } while (false);


            Result result = Result.getSuccess();
            List<ContractTransactionDto> infoDtoList = new ArrayList<>();
            Page<ContractTransactionDto> page = new Page<>(pageNumber, pageSize, txInfoPoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                result.setData(page);
                return result.toRpcClientResult();
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            //List<ContractTransactionInfoDto> resultList = new ArrayList<>();
            if(txInfoPoList.size() > 0) {
                txInfoPoList.sort(new Comparator<TransactionInfoPo>() {
                    @Override
                    public int compare(TransactionInfoPo o1, TransactionInfoPo o2) {
                        return o1.compareTo(o2.getTime());
                    }
                });

                for (int i = start; i < end; i++) {
                    TransactionInfoPo info = txInfoPoList.get(i);
                    RpcClientResult txResult = this.getContractTx(info.getTxHash().getDigestHex());
                    if (txResult.isFailed()) {
                        continue;
                    }
                    infoDtoList.add((ContractTransactionDto) txResult.getData());

                }
            }
            page.setList(infoDtoList);

            result.setSuccess(true);
            result.setData(page);

            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
            return result.toRpcClientResult();
        }
    }

    private boolean checkEquals(byte[] addresses, byte[] desc, int index) {
        try {
            int totalLength = addresses.length;
            int addressLength = Address.ADDRESS_LENGTH;
            int totalCount = totalLength / addressLength;
            int continuousHits = 0;

            for(int i = index, k = 0, flag = i, length = addressLength, count = 0; k < length && count < totalCount;) {
                if(addresses[i] != desc[k]) {
                    k = 0;
                    i = flag + addressLength;
                    flag = i;
                    continuousHits = 0;
                    count++;
                    continue;
                } else {
                    continuousHits++;
                }

                if(continuousHits == addressLength) {
                    return true;
                }
                i++;
                k++;
            }
            return false;
        } catch (Exception e) {
            Log.error("check relative addresses error.", e);
            return false;
        }
    }

    @GET
    @Path("/token/list/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取NRC20合约的资产列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractTokenInfoDto.class)
    })
    public RpcClientResult getTokenList(
            @ApiParam(name="address", value="钱包账户地址", required = true)
            @PathParam("address") String address,
            @ApiParam(name = "pageNumber", value = "页码", required = true)
            @QueryParam("pageNumber") Integer pageNumber,
            @ApiParam(name = "pageSize", value = "每页条数", required = false)
            @QueryParam("pageSize") Integer pageSize) {
        try {
            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            if (StringUtils.isBlank(address)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }

            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            Result<List<ContractTokenInfo>> tokenListResult = contractBalanceManager.getAllTokensByAccount(address);
            if(tokenListResult.isFailed()) {
                return tokenListResult.toRpcClientResult();
            }

            List<ContractTokenInfo> tokenInfoList = tokenListResult.getData();

            Result result = Result.getSuccess();
            List<ContractTokenInfoDto> tokenInfoDtoList = new ArrayList<>();
            Page<ContractTokenInfoDto> page = new Page<>(pageNumber, pageSize, tokenInfoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                result.setData(page);
                return result.toRpcClientResult();
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            if(tokenInfoList.size() > 0) {
                for (int i = start; i < end; i++) {
                    ContractTokenInfo info = tokenInfoList.get(i);
                    tokenInfoDtoList.add(new ContractTokenInfoDto(info));
                }
            }
            if(tokenInfoDtoList != null && tokenInfoDtoList.size() > 0) {
                byte[] prevStateRoot = ContractUtil.getStateRoot(NulsContext.getInstance().getBestBlock().getHeader());
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                for(ContractTokenInfoDto tokenInfo : tokenInfoDtoList) {
                    tokenInfo.setStatus(track.status(AddressTool.getAddress(tokenInfo.getContractAddress())).ordinal());
                }
            }
            page.setList(tokenInfoDtoList);

            result.setSuccess(true);
            result.setData(page);

            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
            return result.toRpcClientResult();
        }
    }

    @POST
    @Path("/collection")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "收藏智能合约地址/修改备注名称")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult contractCollection(@ApiParam(name = "collection", value = "收藏智能合约地址/修改备注名称", required = true) ContractCollection collection) {
        try {
            if (collection == null) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER).toRpcClientResult();
            }

            // 钱包账户地址
            String address = collection.getAccountAddress();
            if (StringUtils.isBlank(address)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }
            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            // 合约地址
            String contractAddress = collection.getContractAddress();
            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
            }

            // 备注名称
            String remarkName = collection.getRemarkName();

            // 获取合约地址的基本信息, 用到两个信息 - 创建者、创建交易hash
            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();

            if(contractAddressInfoPo == null) {
                return Result.getFailed(ContractErrorCode.DATA_NOT_FOUND).toRpcClientResult();
            }

            // 获取该合约的收藏数据
            Result<ContractCollectionInfoPo> collectionInfoPoResult = contractCollectionStorageService.getContractAddress(contractAddressBytes);
            ContractCollectionInfoPo po = collectionInfoPoResult.getData();
            Map<String, String> collectorMap;
            if(po != null) {
                collectorMap = po.getCollectorMap();
                if(collectorMap.containsKey(address)) {
                    String preRemarkName = collectorMap.get(address);
                    if(preRemarkName.equals(remarkName)) {
                        // 收藏者的信息没有变化，直接返回
                        return Result.getSuccess().toRpcClientResult();
                    }
                } else {
                    collectorMap.put(address, EMPTY);
                }

            } else {
                po = new ContractCollectionInfoPo();
                po.setCreater(contractAddressInfoPo.getSender());
                po.setContractAddress(contractAddress);
                po.setBlockHeight(contractAddressInfoPo.getBlockHeight());
                Transaction tx = ledgerService.getTx(contractAddressInfoPo.getCreateTxHash());
                if(tx == null) {
                    return Result.getFailed(ContractErrorCode.TX_NOT_EXIST).toRpcClientResult();
                }
                po.setCreateTime(tx.getTime());
                collectorMap = MapUtil.createHashMap(4);
                collectorMap.put(address, EMPTY);
                po.setCollectorMap(collectorMap);
            }

            // 备注名
            if(StringUtils.isNotBlank(remarkName)) {
                //if (!StringUtils.validAlias(remarkName)) {
                //    return Result.getFailed(ContractErrorCode.CONTRACT_NAME_FORMAT_INCORRECT).toRpcClientResult();
                //}
                collectorMap.put(address, remarkName);
            }

            Result result = contractCollectionStorageService.saveContractAddress(contractAddressBytes, po);
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

    @POST
    @Path("/collection/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "取消收藏智能合约地址")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult collectionCancel(@ApiParam(name = "collectionBase", value = "取消收藏参数", required = true) ContractAddressBase collection ) {
        try {
            // 钱包账户地址
            String address = collection.getAccountAddress();
            if (StringUtils.isBlank(address)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }
            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            // 合约地址
            String contractAddress = collection.getContractAddress();
            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST).toRpcClientResult();
            }

            // 获取该合约的收藏数据
            Result<ContractCollectionInfoPo> collectionInfoPoResult = contractCollectionStorageService.getContractAddress(contractAddressBytes);
            ContractCollectionInfoPo po = collectionInfoPoResult.getData();
            Map<String, String> collectorMap;
            if(po != null) {
                collectorMap = po.getCollectorMap();
                collectorMap.remove(address);
            }
            Result result = contractCollectionStorageService.saveContractAddress(contractAddressBytes, po);
            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

    @GET
    @Path("/wallet/list/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取钱包账户的合约地址列表(账户创建的合约以及钱包收藏的合约)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = ContractAddressDto.class)
    })
    public RpcClientResult getContractCollectionList(
                            @ApiParam(name="address", value="钱包账户地址", required = true)
                            @PathParam("address") String address,
                            @ApiParam(name = "pageNumber", value = "页码", required = true)
                            @QueryParam("pageNumber") Integer pageNumber,
                            @ApiParam(name = "pageSize", value = "每页条数", required = false)
                            @QueryParam("pageSize") Integer pageSize) {
        try {
            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            if (StringUtils.isBlank(address)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }

            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            byte[] addressBytes = AddressTool.getAddress(address);


            LinkedHashMap<String, ContractAddressDto> resultMap = new LinkedHashMap<>();
            // 该账户创建的未确认的合约
            LinkedList<Map<String, String>> list = contractTxService.getLocalUnconfirmedCreateContractTransaction(address);
            if(list != null) {
                String contractAddress;
                Long time;
                ContractAddressDto dto;
                String success;
                for(Map<String, String> map : list) {
                    contractAddress = map.get("contractAddress");
                    time = Long.valueOf(map.get("time"));
                    dto = new ContractAddressDto();
                    dto.setCreate(true);
                    dto.setContractAddress(contractAddress);
                    dto.setCreateTime(time);

                    success = map.get("success");
                    if(StringUtils.isNotBlank(success)) {
                        // 合约创建失败
                        dto.setStatus(3);
                        dto.setMsg(map.get("msg"));
                    } else {
                        dto.setStatus(0);
                    }
                    resultMap.put(contractAddress, dto);
                }
            }

            byte[] prevStateRoot = ContractUtil.getStateRoot(NulsContext.getInstance().getBestBlock().getHeader());

            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            byte[] contractAddressBytes;
            String contractAddress;

            // 获取该账户创建的合约地址
            Result<List<ContractAddressInfoPo>> contractInfoListResult = contractAddressStorageService.getContractInfoList(addressBytes);

            List<ContractAddressInfoPo> contractAddressInfoPoList = contractInfoListResult.getData();
            if(contractAddressInfoPoList != null && contractAddressInfoPoList.size() > 0) {
                contractAddressInfoPoList.sort(new Comparator<ContractAddressInfoPo>() {
                    @Override
                    public int compare(ContractAddressInfoPo o1, ContractAddressInfoPo o2) {
                        return o1.compareTo(o2.getCreateTime());
                    }
                });

                for(ContractAddressInfoPo po : contractAddressInfoPoList) {
                    contractAddressBytes = po.getContractAddress();
                    contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
                    Result<ContractCollectionInfoPo> contractCollectionInfoPoResult = contractCollectionStorageService.getContractAddress(contractAddressBytes);
                    ContractCollectionInfoPo infoPo = contractCollectionInfoPoResult.getData();
                    if(infoPo == null) {
                        resultMap.put(contractAddress, new ContractAddressDto(po, true, track.status(contractAddressBytes).ordinal()));
                    } else {
                        resultMap.put(contractAddress, new ContractAddressDto(infoPo, address, true, track.status(contractAddressBytes).ordinal()));
                    }
                }

            }

            // 获取收藏的合约地址
            List<ContractCollectionInfoPo> contractCollectionPos = getContractAddressCollection(addressBytes);
            if(contractCollectionPos.size() > 0) {
                contractCollectionPos.sort(new Comparator<ContractCollectionInfoPo>() {
                    @Override
                    public int compare(ContractCollectionInfoPo o1, ContractCollectionInfoPo o2) {
                        return o1.compareTo(o2.getCreateTime());
                    }
                });
                for(ContractCollectionInfoPo po : contractCollectionPos) {
                    contractAddress = po.getContractAddress();
                    if(resultMap.containsKey(contractAddress)) {
                        continue;
                    }
                    contractAddressBytes = AddressTool.getAddress(contractAddress);
                    resultMap.put(contractAddress, new ContractAddressDto(po, address, false, track.status(contractAddressBytes).ordinal()));
                }
            }

            List<ContractAddressDto> infoList = new ArrayList<>(resultMap.values());

            Result result = Result.getSuccess();
            List<ContractAddressDto> contractAddressDtoList = new ArrayList<>();
            Page<ContractAddressDto> page = new Page<>(pageNumber, pageSize, infoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                result.setData(page);
                return result.toRpcClientResult();
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            if(infoList.size() > 0) {
                for (int i = start; i < end; i++) {
                    contractAddressDtoList.add(infoList.get(i));
                }
            }
            page.setList(contractAddressDtoList);

            result.setSuccess(true);
            result.setData(page);

            return result.toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

    @POST
    @Path("/unconfirmed/failed/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "删除创建失败的未确认合约")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult removeFailedUnconfirmed(@ApiParam(name = "ContractAddressBase", value = "删除未确认的合约", required = true)
                                             ContractAddressBase addressBase) {
        try {
            // 钱包账户地址
            String address = addressBase.getAccountAddress();
            if (StringUtils.isBlank(address)) {
                return Result.getFailed(LedgerErrorCode.NULL_PARAMETER).toRpcClientResult();
            }
            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult.toRpcClientResult();
            }

            // 合约地址
            String contractAddress = addressBase.getContractAddress();
            if (!AddressTool.validAddress(contractAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
            }

            contractTxService.removeLocalFailedUnconfirmedCreateContractTransaction(address, contractAddress);
            return Result.getSuccess().toRpcClientResult();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
    }

    /**
     * 排除钱包账户自己创建的合约地址
     *
     * @return
     * @param address
     */
    private List<ContractCollectionInfoPo> getContractAddressCollection(byte[] address) {
        // 所有收藏的合约列表
        Result<List<ContractCollectionInfoPo>> contractAddressList = contractCollectionStorageService.getContractAddressList();
        List<ContractCollectionInfoPo> contractCollectionPos = contractAddressList.getData();
        if (contractCollectionPos == null) {
            return new ArrayList<>();
        }
        // 钱包账户
        List<ContractCollectionInfoPo> result = new ArrayList<>();
        for(ContractCollectionInfoPo po : contractCollectionPos) {
            // 排除合约创建者是钱包地址的
            if (Arrays.equals(po.getCreater(), address)) {
                continue;
            }
            if(po.getCollectorMap().containsKey(AddressTool.getStringAddressByBytes(address))) {
                result.add(po);
            }
        }
        return result;
    }


    @POST
    @Path("/upload/constructor")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "上传jar包返回代码构造函数")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RpcClientResult.class)
    })
    public RpcClientResult upload(@ApiParam(name = "jarfile", value = "智能合约代码jar包", required = true)@FormDataParam("jarfile") InputStream jarfile) {
        if (null == jarfile) {
            return Result.getFailed(AccountErrorCode.NULL_PARAMETER).toRpcClientResult();
        }
        try {
            byte[] contractCode = IOUtils.toByteArray(jarfile);
            ContractInfoDto contractInfoDto = vmHelper.getConstructor(contractCode);
            if(contractInfoDto == null || contractInfoDto.getConstructor() == null) {
                return Result.getFailed(ContractErrorCode.ILLEGAL_CONTRACT).toRpcClientResult();
            }
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("constructor", contractInfoDto.getConstructor());
            resultMap.put("isNrc20", contractInfoDto.isNrc20());
            resultMap.put("code", Hex.encode(contractCode));
            return Result.getSuccess().setData(resultMap).toRpcClientResult();
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(ContractErrorCode.DATA_ERROR).toRpcClientResult();
        }
    }


    @GET
    @Path("/export/{address}")
    @ApiOperation(value = "导出合约编译代码的jar包 ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public void export(@ApiParam(name = "address", value = "账户地址", required = true)
                                  @PathParam("address") String address,
                                           @Context HttpServletResponse response) {
        try {
            if (StringUtils.isBlank(address)) {
                return;
            }

            if (!AddressTool.validAddress(address)) {
                return;
            }

            byte[] contractAddressBytes = AddressTool.getAddress(address);
            if(!ContractLedgerUtil.isExistContractAddress(contractAddressBytes)) {
                return;
            }
            byte[] addressBytes = AddressTool.getAddress(address);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractAddressStorageService.getContractAddressInfo(addressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if(po == null) {
                return;
            }
            Transaction tx = ledgerService.getTx(po.getCreateTxHash());
            CreateContractTransaction create = (CreateContractTransaction) tx;
            CreateContractData createTxData = create.getTxData();
            byte[] code = createTxData.getCode();

            //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("application/octet-stream");
            //2.设置文件头：最后一个参数是设置下载文件名
            response.addHeader("Content-Disposition", "attachment;filename=" + address + ".jar");
            response.getOutputStream().write(code);
            response.getOutputStream().flush();
        } catch (Exception e) {
            Log.error("Export Exception!");
        }
    }

}
