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

import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.util.ContractUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/19
 */
@ApiModel(value = "合约token资产信息")
public class ContractTokenInfoDto {

    @ApiModelProperty(name = "contractAddress", value = "合约地址")
    private String contractAddress;

    @ApiModelProperty(name = "name", value = "token名称")
    private String name;

    @ApiModelProperty(name = "symbol", value = "token符号")
    private String symbol;

    @ApiModelProperty(name = "amount", value = "token数量")
    private String amount;

    @ApiModelProperty(name = "decimals", value = "token支持的小数位数")
    private long decimals;

    @ApiModelProperty(name = "blockHeight", value = "合约创建时的区块高度")
    private long blockHeight;

    @ApiModelProperty(name = "status", value = "合约状态(0-不存在, 1-正常, 2-终止)")
    private int status;

    public ContractTokenInfoDto() {
    }

    public ContractTokenInfoDto(ContractTokenInfo info) {
        this.contractAddress = info.getContractAddress();
        this.name = info.getName();
        this.symbol = info.getSymbol();
        this.amount = ContractUtil.bigInteger2String(info.getAmount());
        this.decimals = info.getDecimals();
        this.blockHeight = info.getBlockHeight();
        this.status = info.getStatus();
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
