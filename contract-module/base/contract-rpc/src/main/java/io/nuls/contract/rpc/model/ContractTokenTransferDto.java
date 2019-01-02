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

import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.util.ContractUtil;
import io.nuls.kernel.utils.AddressTool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 */
@ApiModel(value = "ContractTokenTransferDtoJSON")
public class ContractTokenTransferDto {

    @ApiModelProperty(name = "contractAddress", value = "合约地址")
    private String contractAddress;
    @ApiModelProperty(name = "from", value = "付款方")
    private String from;
    @ApiModelProperty(name = "to", value = "收款方")
    private String to;
    @ApiModelProperty(name = "value", value = "转账金额")
    private String value;
    @ApiModelProperty(name = "name", value = "token名称")
    private String name;
    @ApiModelProperty(name = "symbol", value = "token符号")
    private String symbol;
    @ApiModelProperty(name = "decimals", value = "token支持的小数位数")
    private long decimals;

    public ContractTokenTransferDto(ContractTokenTransferInfoPo po) {
        this.contractAddress = po.getContractAddress();
        if(po.getFrom() != null) {
            this.from = AddressTool.getStringAddressByBytes(po.getFrom());
        }
        if(po.getTo() != null) {
            this.to = AddressTool.getStringAddressByBytes(po.getTo());
        }
        this.value = ContractUtil.bigInteger2String(po.getValue());
        this.name = po.getName();
        this.symbol = po.getSymbol();
        this.decimals = po.getDecimals();
    }

    public void setNrc20Info(ContractAddressInfoPo po) {
        this.name = po.getNrc20TokenName();
        this.symbol = po.getNrc20TokenSymbol();
        this.decimals = po.getDecimals();
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
