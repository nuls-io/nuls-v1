package io.nuls.contract.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "向智能合约转账手续费")
public class ContractTransferFee {

    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;
    @ApiModelProperty(name = "toAddress", value = "账户地址(合约地址)", required = true)
    private String toAddress;
    @ApiModelProperty(name = "gasLimit", value = "最大gas消耗", required = true)
    private long gasLimit;
    @ApiModelProperty(name = "price", value = "执行合约单价", required = true)
    private long price;
    @ApiModelProperty(name = "amount", value = "转账金额", required = true)
    private long amount;
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

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
