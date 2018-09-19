package io.nuls.contract.rpc.form;

import io.nuls.contract.util.ContractUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/4/20
 */
@ApiModel(value = "测试创建智能合约表单数据")
public class PreContractCreate {

    @ApiModelProperty(name = "sender", value = "交易创建者", required = true)
    private String sender;
    @ApiModelProperty(name = "gasLimit", value = "最大gas消耗", required = true)
    private long gasLimit;
    @ApiModelProperty(name = "price", value = "执行合约单价", required = true)
    private long price;
    @ApiModelProperty(name = "contractCode", value = "智能合约代码(字节码的Hex编码字符串)", required = true)
    private String contractCode;
    @ApiModelProperty(name = "args", value = "参数列表", required = false)
    private Object[] args;
    @ApiModelProperty(name = "remark", value = "备注", required = false)
    private String remark;

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public String[][] getArgs(String[] types) {
        return ContractUtil.twoDimensionalArray(args, types);
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
