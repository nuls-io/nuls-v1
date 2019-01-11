/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.rpc.model;

import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.txdata.ContractData;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.tools.calc.LongUtils;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: PierreLuo
 */
@ApiModel(value = "contractResultDtoJSON")
public class ContractResultDto {

    @ApiModelProperty(name = "success", value = "合约执行是否成功")
    private boolean success;

    @ApiModelProperty(name = "errorMessage", value = "执行失败信息")
    private String errorMessage;

    @ApiModelProperty(name = "contractAddress", value = "合约地址")
    private String contractAddress;

    @ApiModelProperty(name = "result", value = "合约执行结果")
    private String result;

    @ApiModelProperty(name = "gasLimit", value = "GasLimit")
    private long gasLimit;

    @ApiModelProperty(name = "gasUsed", value = "已使用Gas")
    private long gasUsed;

    @ApiModelProperty(name = "price", value = "单价")
    private long price;

    @ApiModelProperty(name = "totalFee", value = "交易总手续费")
    private BigInteger totalFee;

    @ApiModelProperty(name = "txSizeFee", value = "交易大小手续费")
    private BigInteger txSizeFee;

    @ApiModelProperty(name = "actualContractFee", value = "实际执行合约手续费")
    private BigInteger actualContractFee;

    @ApiModelProperty(name = "refundFee", value = "退还的手续费")
    private BigInteger refundFee;

    @ApiModelProperty(name = "stateRoot", value = "状态根")
    private String stateRoot;

    @ApiModelProperty(name = "value", value = "交易附带的货币量")
    private long value;

    @ApiModelProperty(name = "stackTrace", value = "堆栈踪迹")
    private String stackTrace;

    @ApiModelProperty(name = "balance", value = "合约余额")
    private BigInteger balance;

    @ApiModelProperty(name = "nonce", value = "nonce")
    private BigInteger nonce;

    @ApiModelProperty(name = "transfers", value = "合约转账(从合约转出)")
    private List<ContractTransferDto> transfers;

    @ApiModelProperty(name = "events", value = "合约事件")
    private List<String> events;

    @ApiModelProperty(name = "tokenTransfers", value = "合约代币转账")
    private List<ContractTokenTransferDto> tokenTransfers;

    @ApiModelProperty(name = "name", value = "token名称")
    private String name;

    @ApiModelProperty(name = "symbol", value = "token符号")
    private String symbol;

    @ApiModelProperty(name = "decimals", value = "token支持的小数位数")
    private long decimals;

    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    public ContractResultDto() {}

    public ContractResultDto(ContractResult result, Transaction tx) {
        ContractData contractData = (ContractData) tx.getTxData();
        this.totalFee = BigInteger.valueOf(tx.getFee().getValue());
        this.gasLimit = contractData.getGasLimit();
        this.gasUsed = result.getGasUsed();
        this.price = result.getPrice();
        this.actualContractFee = BigInteger.valueOf(LongUtils.mul(this.gasUsed, this.price));
        BigInteger contractFee = BigInteger.valueOf(LongUtils.mul(gasLimit, price));
        this.refundFee = contractFee.subtract(this.actualContractFee);
        this.txSizeFee = this.totalFee.subtract(contractFee);
        this.contractAddress = AddressTool.getStringAddressByBytes(result.getContractAddress());
        this.result = result.getResult();
        this.stateRoot = (result.getStateRoot() != null ? Hex.encode(result.getStateRoot()) : null);
        this.value = result.getValue();
        this.success = result.isSuccess();
        this.errorMessage = result.getErrorMessage();
        this.stackTrace = result.getStackTrace();
        this.balance = result.getBalance();
        this.nonce = result.getNonce();
        this.setOrginTransfers(result.getTransfers());
        this.events = result.getEvents();
        this.remark = result.getRemark();
        if(result.isSuccess()) {
            this.makeTokenTransfers(result.getEvents());
        }
    }

    public ContractResultDto(ContractResult result, Transaction tx, ContractAddressInfoPo po) {
        this(result, tx);
        if(result.isNrc20()) {
            this.name = po.getNrc20TokenName();
            this.symbol = po.getNrc20TokenSymbol();
            this.decimals = po.getDecimals();
        }
    }

    public ContractResultDto(ContractResult contractExecuteResult, Transaction tx, ContractAddressInfoPo po, ContractTokenTransferInfoPo transferInfoPo) {
        this(contractExecuteResult, tx, po);
        if(transferInfoPo != null) {
            this.tokenTransfers = new ArrayList<>();
            this.tokenTransfers.add(new ContractTokenTransferDto(transferInfoPo));
        }
    }

    public List<ContractTokenTransferDto> getTokenTransfers() {
        return tokenTransfers == null ? new ArrayList<>() : tokenTransfers;
    }

    public void setTokenTransfers(List<ContractTokenTransferDto> tokenTransfers) {
        this.tokenTransfers = tokenTransfers;
    }

    private void makeTokenTransfers(List<String> tokenTransferEvents) {
        List<ContractTokenTransferDto> result = new ArrayList<>();
        if(tokenTransferEvents != null && tokenTransferEvents.size() > 0) {
            ContractTokenTransferInfoPo po;
            for(String event : tokenTransferEvents) {
                po = ContractUtil.convertJsonToTokenTransferInfoPo(event);
                if(po != null) {
                    result.add(new ContractTokenTransferDto(po));
                }
            }
        }
        this.tokenTransfers = result;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public List<ContractTransferDto> getTransfers() {
        return transfers == null ? new ArrayList<>() : transfers;
    }

    public void setTransfers(List<ContractTransferDto> transfers) {
        this.transfers = transfers;
    }

    public void setOrginTransfers(List<ContractTransfer> transfers) {
        List<ContractTransferDto> list = new LinkedList<>();
        this.transfers = list;
        if(transfers == null || transfers.size() == 0) {
            return;
        }
        for(ContractTransfer transfer : transfers) {
            list.add(new ContractTransferDto(transfer));
        }
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigInteger totalFee) {
        this.totalFee = totalFee;
    }

    public BigInteger getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigInteger refundFee) {
        this.refundFee = refundFee;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public BigInteger getActualContractFee() {
        return actualContractFee;
    }

    public void setActualContractFee(BigInteger actualContractFee) {
        this.actualContractFee = actualContractFee;
    }

    public BigInteger getTxSizeFee() {
        return txSizeFee;
    }

    public void setTxSizeFee(BigInteger txSizeFee) {
        this.txSizeFee = txSizeFee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }
}
