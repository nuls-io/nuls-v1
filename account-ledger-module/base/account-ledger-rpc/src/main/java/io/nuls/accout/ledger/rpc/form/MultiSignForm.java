package io.nuls.accout.ledger.rpc.form;
import io.nuls.accout.ledger.rpc.dto.MultipleTxToDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author tangag
 */
@ApiModel(value = "创建多重签名交易")
public class MultiSignForm {
    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "outputs", value = "交易输出", required = true)
    private List<MultipleTxToDto> outputs;

    @ApiModelProperty(name = "password", value = "账户密码", required = false)
    private String password;

    @ApiModelProperty(name = "amount", value = "金额", required = true)
    private long amount;

    @ApiModelProperty(name = "remark", value = "交易备注")
    private String remark;

    @ApiModelProperty(name = "pubkeys", value = "需要签名的公钥列表", required = true)
    private List<String> pubkeys;

    @ApiModelProperty(name = "m", value = "至少需要几个公钥验证通过", required = true)
    private int m;

    @ApiModelProperty(name = "txdata", value = "交易数据")
    private String txdata;

    public MultiSignForm() {

    }

    public List<String> getPubkeys() {
        return pubkeys;
    }

    public void setPubkeys(List<String> pubkeys) {
        this.pubkeys = pubkeys;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public String getTxdata() {
        return txdata;
    }

    public void setTxdata(String txdata) {
        this.txdata = txdata;
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
}
