package io.nuls.accout.ledger.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author tag
 */
@ApiModel(value = "多签账户转账表单form")
public class MultiTransactionSignForm {
    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "password", value = "账户密码", required = false)
    private String password;

    @ApiModelProperty(name = "txdata", value = "交易数据")
    private String txdata;

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTxdata() {
        return txdata;
    }

    public void setTxdata(String txdata) {
        this.txdata = txdata;
    }
}
