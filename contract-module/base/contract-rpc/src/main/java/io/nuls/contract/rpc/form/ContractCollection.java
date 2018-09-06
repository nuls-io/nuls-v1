package io.nuls.contract.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/15
 */
@ApiModel(value = "智能合约收藏列表")
public class ContractCollection extends ContractAddressBase {

    @ApiModelProperty(name = "remarkName", value = "备注名", required = false)
    private String remarkName;

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }
}
