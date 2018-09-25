package io.nuls.contract.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;

@ApiModel(value = "token转账")
public class ContractTokenTransfer {

    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;
    @ApiModelProperty(name = "toAddress", value = "账户地址", required = true)
    private String toAddress;
    @ApiModelProperty(name = "contractAddress", value = "合约地址", required = true)
    private String contractAddress;
    @ApiModelProperty(name = "gasLimit", value = "最大gas消耗", required = true)
    private long gasLimit;
    @ApiModelProperty(name = "price", value = "执行合约单价", required = true)
    private long price;
    @ApiModelProperty(name = "password", value = "账户密码", required = false)
    private String password;
    @ApiModelProperty(name = "amount", value = "转账金额", required = true)
    private String amount;
    @ApiModelProperty(name = "remark", value = "备注", required = false)
    private String remark;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
