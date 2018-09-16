package io.nuls.consensus.poc.rpc.model;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author tag
 */
@ApiModel(value = "多签账户创建共识(代理)节点表单数据")
public class CreateMutilAgentForm {
    @ApiModelProperty(name = "agentAddress", value = "申请账户的地址", required = true)
    private String agentAddress;

    @ApiModelProperty(name = "packingAddress", value = "打包地址", required = true)
    private String packingAddress;
    @ApiModelProperty(name = "rewardAddress", value = "结算地址", required = false)
    private String rewardAddress;

    @ApiModelProperty(name = "commissionRate", value = "佣金比例", required = true)
    private double commissionRate;

    @ApiModelProperty(name = "deposit", value = "参与共识需要的总金额", required = true)
    private long deposit;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "password", value = "密码", required = true)
    private String password;

    @ApiModelProperty(name = "pubkeys", value = "需要签名的公钥列表", required = true)
    private List<String> pubkeys;

    @ApiModelProperty(name = "m", value = "至少需要几个公钥验证通过", required = true)
    private int m;

    @ApiModelProperty(name = "txdata", value = "交易数据")
    private String txdata;

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = StringUtils.formatStringPara(agentAddress);
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = StringUtils.formatStringPara(packingAddress);
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
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
