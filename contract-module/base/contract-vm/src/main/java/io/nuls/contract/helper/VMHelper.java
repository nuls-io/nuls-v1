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
package io.nuls.contract.helper;

import io.nuls.account.service.AccountService;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.ledger.manager.ContractBalanceManager;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.service.DBService;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.contract.constant.ContractConstant.*;

@Component
public class VMHelper implements InitializingBean {

    @Autowired
    private VMContext vmContext;
    @Autowired
    private DBService dbService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ContractBalanceManager contractBalanceManager;
    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    private ProgramExecutor programExecutor;

    private ConcurrentHashMap<String, Long> accountLastedPriceMap = MapUtil.createConcurrentHashMap(4);

    @Override
    public void afterPropertiesSet() throws NulsException {
        programExecutor = new ProgramExecutorImpl(vmContext, dbService);
    }

    public ProgramExecutor getProgramExecutor() {
        return programExecutor;
    }

    public boolean checkIsViewMethod(String methodName, byte[] contractAddressBytes) {
        BlockHeader header = NulsContext.getInstance().getBestBlock().getHeader();
        // 当前区块状态根
        byte[] currentStateRoot = header.getStateRoot();

        ProgramExecutor track = programExecutor.begin(currentStateRoot);
        List<ProgramMethod> methods = track.method(contractAddressBytes);

        boolean isViewMethod = false;
        if(methods != null) {
            for(ProgramMethod method : methods) {
                if(methodName.equals(method.getName())) {
                    isViewMethod = method.isView();
                    break;
                }
            }
        }
        return isViewMethod;
    }

    public ProgramMethod getConstructor(byte[] contractCode) {
        try {
            List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);
            if(programMethods == null || programMethods.size() == 0) {
                return null;
            }
            for(ProgramMethod method : programMethods) {
                if(ContractConstant.CONTRACT_CONSTRUCTOR.equals(method.getName())) {
                    return method;
                }
            }
            return null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public ProgramResult invokeViewMethod(byte[] contractAddressBytes, String methodName, String methodDesc, Object... args) {
        return this.invokeViewMethod(contractAddressBytes, methodName, methodDesc, ContractUtil.twoDimensionalArray(args));
    }

    public ProgramResult invokeViewMethod(byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {
        // 当前区块高度
        BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
        long blockHeight = blockHeader.getHeight();
        // 当前区块状态根
        byte[] currentStateRoot = blockHeader.getStateRoot();

        return this.invokeViewMethod(currentStateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, args);
    }

    private ProgramResult invokeViewMethod(byte[] stateRoot, long blockHeight, byte[] contractAddressBytes, String methodName, String methodDesc, Object... args) {
        return this.invokeViewMethod(stateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, ContractUtil.twoDimensionalArray(args));
    }

    public ProgramResult invokeViewMethod(byte[] stateRoot, long blockHeight, byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddressBytes);
        programCall.setValue(BigInteger.ZERO);
        programCall.setGasLimit(ContractConstant.CONTRACT_CONSTANT_GASLIMIT);
        programCall.setPrice(ContractConstant.CONTRACT_CONSTANT_PRICE);
        programCall.setNumber(blockHeight);
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);

        ProgramExecutor track = programExecutor.begin(stateRoot);
        ProgramResult programResult = track.call(programCall);

        return programResult;
    }

    public void updateLastedPriceForAccount(byte[] sender, long price) {
        if(price <= 0) {
            return;
        }
        String address = AddressTool.getStringAddressByBytes(sender);
        accountLastedPriceMap.put(address, price);
    }

    public long getLastedPriceForAccount(byte[] sender) {
        String address = AddressTool.getStringAddressByBytes(sender);
        Long price = accountLastedPriceMap.get(address);
        if(price == null) {
            price = 20L;
            accountLastedPriceMap.put(address, price);
        }
        return price;
    }

    public void dealEvents(Transaction tx, ContractResult contractResult, ContractAddressInfoPo po) {
        if(po == null) {
            return;
        }
        // 非代币事件不处理
        if(!po.isNrc20()) {
            return;
        }
        try {

            byte[] stateRoot = contractResult.getStateRoot();
            byte[] contractAddress = contractResult.getContractAddress();
            String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);
            List<String> events = contractResult.getEvents();
            int size = events.size();
            // 目前只处理Transfer事件, 为了刷新账户的token余额
            String event;
            if(events != null && size > 0) {
                for(int i = 0; i < size; i++) {
                    event = events.get(i);
                    // 按照NRC20标准，TransferEvent事件中第一个参数是转出地址-from，第二个参数是转入地址-to, 第三个参数是金额
                    ContractTokenTransferInfoPo tokenTransferInfoPo = ContractUtil.convertJsonToTokenTransferInfoPo(event);
                    if(tokenTransferInfoPo == null) {
                        continue;
                    }
                    byte[] txHashBytes = null;
                    byte[] from = tokenTransferInfoPo.getFrom();
                    byte[] to = tokenTransferInfoPo.getTo();
                    tokenTransferInfoPo.setName(po.getNrc20TokenName());
                    tokenTransferInfoPo.setSymbol(po.getNrc20TokenSymbol());
                    tokenTransferInfoPo.setDecimals(po.getDecimals());
                    tokenTransferInfoPo.setTime(tx.getTime());
                    tokenTransferInfoPo.setContractAddress(contractAddress);
                    tokenTransferInfoPo.setBlockHeight(tx.getBlockHeight());
                    txHashBytes = tx.getHash().serialize();
                    tokenTransferInfoPo.setTxHash(txHashBytes);
                    tokenTransferInfoPo.setStatus((byte) (contractResult.isSuccess() ? 1 : 2));

                    // byte[] outKey = ArraysTool.concatenate(tx.getHash().serialize(), new VarInt(i).encode());
                    if(from != null) {
                        this.refreshTokenBalance(stateRoot, po, AddressTool.getStringAddressByBytes(from), contractAddressStr);
                        this.saveTokenTransferInfo(from, txHashBytes, new VarInt(i).encode(), tokenTransferInfoPo);
                    }
                    if(to != null) {
                        this.refreshTokenBalance(stateRoot, po, AddressTool.getStringAddressByBytes(to), contractAddressStr);
                        this.saveTokenTransferInfo(to, txHashBytes, new VarInt(i).encode(), tokenTransferInfoPo);
                    }
                }
            }
        } catch (Exception e) {
            Log.warn("contract event parse error.", e);
        }
    }

    private void saveTokenTransferInfo(byte[] address, byte[] txHashBytes, byte[] index, ContractTokenTransferInfoPo tokenTransferInfoPo) {
        contractTokenTransferStorageService.saveTokenTransferInfo(ArraysTool.concatenate(address, txHashBytes, index), tokenTransferInfoPo);
    }

    public void refreshTokenBalance(byte[] stateRoot, ContractAddressInfoPo po, String address, String contractAddress) {
        long blockHeight = po.getBlockHeight();
        long bestBlockHeight = NulsContext.getInstance().getBestHeight();
        String tokenName = po.getNrc20TokenName();
        byte[] contractAddressBytes = po.getContractAddress();
        ProgramResult programResult = this.invokeViewMethod(stateRoot, bestBlockHeight, contractAddressBytes, NRC20_METHOD_BALANCE_OF, null, address);
        Result<ContractTokenInfo> result = null;
        if(!programResult.isSuccess()) {
            return;
        } else {
            contractBalanceManager.refreshContractToken(address, contractAddress, po, new BigInteger(programResult.getResult()));
        }

    }

    private boolean checkNrc20Contract(byte[] stateRoot, byte[] contractAddress) {
        ProgramExecutor track = programExecutor.begin(stateRoot);
        List<ProgramMethod> methods = track.method(contractAddress);
        if(methods == null || methods.size() == 0) {
            return false;
        }
        Map<String, ProgramMethod> contractMethodsMap = MapUtil.createHashMap(methods.size());
        for(ProgramMethod method : methods) {
            contractMethodsMap.put(method.getName(), method);
        }

        Set<Map.Entry<String, ProgramMethod>> entries = VMContext.getNrc20Methods().entrySet();
        String methodName;
        ProgramMethod standardMethod;
        ProgramMethod mappingMethod;
        for(Map.Entry<String, ProgramMethod> entry : entries) {
            methodName = entry.getKey();
            standardMethod = entry.getValue();
            mappingMethod = contractMethodsMap.get(methodName);

            if(mappingMethod == null) {
                return false;
            }
            if(!standardMethod.equalsNrc20Method(mappingMethod)) {
                return false;
            }
        }

        return true;
    }

    public Result validateNrc20Contract(Transaction tx, ContractResult contractResult) {
        if(contractResult == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        byte[] stateRoot = contractResult.getStateRoot();
        byte[] contractAddress = contractResult.getContractAddress();
        long blockHeight = tx.getBlockHeight();
        long bestBlockHeight = NulsContext.getInstance().getBestHeight();
        boolean isNrc20 = this.checkNrc20Contract(stateRoot, contractAddress);
        contractResult.setNrc20(isNrc20);
        if(isNrc20) {
            // NRC20 tokenName 验证代币名称格式
            ProgramResult programResult = this.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, NRC20_METHOD_NAME, null, null);
            if(programResult.isSuccess()) {
                String tokenName = programResult.getResult();
                if(StringUtils.isNotBlank(tokenName)) {
                    if(!StringUtils.validTokenNameOrSymbol(tokenName)) {
                        return Result.getFailed(ContractErrorCode.CONTRACT_NAME_FORMAT_INCORRECT);
                    }
                }
            }
            // NRC20 tokenSymbol 验证代币符号的格式
            programResult = this.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, "symbol", null, null);
            if(programResult.isSuccess()) {
                String symbol = programResult.getResult();
                if(StringUtils.isNotBlank(symbol)) {
                    if(!StringUtils.validTokenNameOrSymbol(symbol)) {
                        return Result.getFailed(ContractErrorCode.CONTRACT_NRC20_SYMBOL_FORMAT_INCORRECT);
                    }
                }
            }
        }
        return Result.getSuccess();
    }

}
