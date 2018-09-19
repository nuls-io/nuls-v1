package io.nuls.contract.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/7/18
 */
@ApiModel(value = "估算智能合约的price")
public class ImputedPrice {

    @ApiModelProperty(name = "sender", value = "交易创建者", required = true)
    private String sender;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

}
