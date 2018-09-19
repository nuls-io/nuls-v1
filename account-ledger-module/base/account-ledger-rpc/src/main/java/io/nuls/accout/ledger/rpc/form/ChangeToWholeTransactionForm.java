package io.nuls.accout.ledger.rpc.form;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "创建零钱汇整交易")
public class ChangeToWholeTransactionForm {

    @ApiModelProperty(name = "address", value = "交易输入", required = true)
    private String address;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

}
