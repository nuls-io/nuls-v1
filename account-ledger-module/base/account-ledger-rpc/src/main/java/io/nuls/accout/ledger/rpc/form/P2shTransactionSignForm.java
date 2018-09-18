package io.nuls.accout.ledger.rpc.form;

import io.nuls.accout.ledger.rpc.dto.MultipleTxToDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author tag
 */
@ApiModel(value = "多签交易签名表单form")
public class P2shTransactionSignForm {
    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "outputs", value = "交易输出", required = true)
    private List<MultipleTxToDto> outputs;

    @ApiModelProperty(name = "password", value = "账户密码", required = false)
    private String password;

    @ApiModelProperty(name = "remark", value = "交易备注")
    private String remark;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }

    public List<MultipleTxToDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MultipleTxToDto> outputs) {
        this.outputs = outputs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
