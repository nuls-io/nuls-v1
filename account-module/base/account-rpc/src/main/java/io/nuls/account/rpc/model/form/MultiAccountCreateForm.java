package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: tangag
 */
@ApiModel(value = "创建多重签名账户表单")
public class MultiAccountCreateForm {
    @ApiModelProperty(name = "pubkeys", value = "需要签名在公钥列表", required = true)
    private List<String> pubkeys;

    @ApiModelProperty(name = "m", value = "至少需要几个公钥验证通过", required = true)
    private int m;

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
}
