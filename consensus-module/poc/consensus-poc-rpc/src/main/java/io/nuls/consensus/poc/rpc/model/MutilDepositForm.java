package io.nuls.consensus.poc.rpc.model;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author tag
 */
@ApiModel(value = "多签账户申请参与共识表单数据")
public class MutilDepositForm {


    @ApiModelProperty(name = "address", value = "参与共识账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "agentHash", value = "共识节点id", required = true)
    private String agentHash;

    @ApiModelProperty(name = "deposit", value = "参与共识的金额", required = true)
    private long deposit;

    @ApiModelProperty(name = "password", value = "密码", required = true)
    private String password;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "pubkeys", value = "需要签名的公钥列表", required = true)
    private List<String> pubkeys;

    @ApiModelProperty(name = "m", value = "至少需要几个公钥验证通过", required = true)
    private int m;

    @ApiModelProperty(name = "txdata", value = "交易数据")
    private String txdata;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = StringUtils.formatStringPara(address);
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = StringUtils.formatStringPara(agentHash);
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
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

    public List<String> getPubkeys() {
        return pubkeys;
    }

    public void setPubkeys(List<String> pubkeys) {
        this.pubkeys = pubkeys;
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
}
