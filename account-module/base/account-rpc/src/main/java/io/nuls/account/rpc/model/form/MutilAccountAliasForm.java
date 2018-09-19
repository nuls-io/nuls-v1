package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: tag
 */
@ApiModel(value = "多签地址设置账户别名表单数据")
public class MutilAccountAliasForm {
    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    private String alias;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

    @ApiModelProperty(name = "pubkeys", value = "需要签名的公钥列表", required = true)
    private List<String> pubkeys;

    @ApiModelProperty(name = "m", value = "至少需要几个公钥验证通过", required = true)
    private int m;

    @ApiModelProperty(name = "txdata", value = "交易数据")
    private String txdata;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = StringUtils.formatStringPara(alias);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
