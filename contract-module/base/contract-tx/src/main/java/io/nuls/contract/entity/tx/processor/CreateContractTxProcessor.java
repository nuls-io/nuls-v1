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
package io.nuls.contract.entity.tx.processor;

import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.entity.tx.CreateContractTransaction;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.contract.helper.VMHelper;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractCollectionStorageService;
import io.nuls.contract.storage.service.ContractExecuteResultStorageService;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.nuls.contract.constant.ContractConstant.*;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
@Component
public class CreateContractTxProcessor implements TransactionProcessor<CreateContractTransaction>, InitializingBean {

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractTxService contractTxService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractExecuteResultStorageService contractExecuteResultStorageService;

    @Autowired
    private ContractCollectionStorageService contractCollectionStorageService;

    @Autowired
    private VMHelper vmHelper;

    private ProgramExecutor programExecutor;

    @Override
    public void afterPropertiesSet() throws NulsException {
        programExecutor = vmHelper.getProgramExecutor();
    }

    @Override
    public Result onRollback(CreateContractTransaction tx, Object secondaryData) {
        CreateContractData txData = tx.getTxData();
        byte[] contractAddress = txData.getContractAddress();
        contractCollectionStorageService.deleteContractAddress(contractAddress);
        contractAddressStorageService.deleteContractAddress(contractAddress);
        contractService.deleteContractExecuteResult(tx.getHash());
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(CreateContractTransaction tx, Object secondaryData) {
        ContractResult contractResult = tx.getContractResult();
        contractService.saveContractExecuteResult(tx.getHash(), contractResult);

        CreateContractData txData = tx.getTxData();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        String senderStr = AddressTool.getStringAddressByBytes(sender);
        String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);
        // 移除未确认的创建合约交易
        contractTxService.removeLocalUnconfirmedCreateContractTransaction(
                senderStr, contractAddressStr, contractResult);

        // 执行失败的合约直接返回
        if(!contractResult.isSuccess()) {
            return Result.getSuccess();
        }

        NulsDigestData hash = tx.getHash();
        long blockHeight = tx.getBlockHeight();
        long bestBlockHeight = NulsContext.getInstance().getBestHeight();
        ContractAddressInfoPo info = new ContractAddressInfoPo();
        info.setContractAddress(contractAddress);
        info.setSender(sender);
        try {
            info.setCreateTxHash(hash.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        info.setCreateTime(tx.getTime());
        info.setBlockHeight(blockHeight);

        byte[] stateRoot = contractResult.getStateRoot();
        boolean isNrc20Contract = contractResult.isNrc20();
        info.setNrc20(isNrc20Contract);
        // 获取 token tracker
        if(isNrc20Contract) {
            // NRC20 token 标准方法获取名称数据
            ProgramResult programResult = vmHelper.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, NRC20_METHOD_NAME, null, null);
            if(programResult.isSuccess()) {
                String tokenName = programResult.getResult();
                info.setNrc20TokenName(tokenName);
            }
            programResult = vmHelper.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, NRC20_METHOD_SYMBOL, null, null);
            if(programResult.isSuccess()) {
                String symbol = programResult.getResult();
                info.setNrc20TokenSymbol(symbol);
            }
            programResult = vmHelper.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, NRC20_METHOD_DECIMALS, null, null);
            if(programResult.isSuccess()) {
                String decimals = programResult.getResult();
                if(StringUtils.isNotBlank(decimals)) {
                    try {
                        info.setDecimals(new BigInteger(decimals).longValue());
                    } catch (Exception e) {
                        Log.error("Get nrc20 decimals error.", e);
                        // skip it
                    }
                }
            }
            programResult = vmHelper.invokeViewMethod(stateRoot, bestBlockHeight, contractAddress, NRC20_METHOD_TOTAL_SUPPLY, null, null);
            if(programResult.isSuccess()) {
                String totalSupply = programResult.getResult();
                if(StringUtils.isNotBlank(totalSupply)) {
                    try {
                        info.setTotalSupply(new BigInteger(totalSupply));
                    } catch (Exception e) {
                        Log.error("Get nrc20 totalSupply error.", e);
                        // skip it
                    }
                }
            }
            // 刷新创建者的token余额
            vmHelper.refreshTokenBalance(stateRoot, info, senderStr, contractAddressStr);
            // 处理合约事件
            vmHelper.dealEvents(tx, contractResult, info);
        }

        Result result = contractAddressStorageService.saveContractAddress(contractAddress, info);
        return result;
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }

}
