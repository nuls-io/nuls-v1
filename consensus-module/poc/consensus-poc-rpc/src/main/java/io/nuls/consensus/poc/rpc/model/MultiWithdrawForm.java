package io.nuls.consensus.poc.rpc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author tag
 */
@ApiModel(value = "多签账户退出共识表单数据")
public class MultiWithdrawForm {
    @ApiModelProperty(name = "address", value = "节点地址", required = true)
    private String address;

    @ApiModelProperty(name = "txHash", value = "加入共识时的交易hash", required = true)
    private String txHash;

    @ApiModelProperty(name = "password", value = "密码", required = true)
    private String password;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
