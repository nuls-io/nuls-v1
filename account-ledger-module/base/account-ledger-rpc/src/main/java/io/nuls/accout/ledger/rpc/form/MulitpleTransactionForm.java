package io.nuls.accout.ledger.rpc.form;


import io.nuls.accout.ledger.rpc.dto.MulipleTxFromDto;
import io.nuls.accout.ledger.rpc.dto.MultipleTxToDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.QueryParam;
import java.util.List;

@ApiModel(value = "创建多账户转账交易form")
public class MulitpleTransactionForm {

    @ApiModelProperty(name = "inputs", value = "交易输入", required = true)
    private List<MulipleTxFromDto> inputs;

    @ApiModelProperty(name = "outputs", value = "交易输出", required = true)
    private List<MultipleTxToDto> outputs;

    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public List<MulipleTxFromDto> getInputs() {
        return inputs;
    }

    public List<MultipleTxToDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MultipleTxToDto> outputs) {
        this.outputs = outputs;
    }

    public void setInputs(List<MulipleTxFromDto> inputs) {
        this.inputs = inputs;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
