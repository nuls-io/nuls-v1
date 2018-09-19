package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * @author: tangag
 */
@ApiModel(value = "计算多重签名别名手续费表单")
public class MultiAliasFeeForm {
    @ApiModelProperty(name = "address", value = "地址", required = true)
    @QueryParam("address")
    private String address;

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    @QueryParam("alias")
    private String alias;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = StringUtils.formatStringPara(alias);
    }
}
