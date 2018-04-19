package io.nuls.rpc.entity;

import io.nuls.ledger.entity.UtxoInput;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "inputJSON")
public class InputDto {

    @ApiModelProperty(name = "index", value = "输入索引")
    private Integer index;

    @ApiModelProperty(name = "fromHash", value = "来源output的txHash")
    private String fromHash;

    @ApiModelProperty(name = "fromIndex", value = "来源output的outIndex")
    private Integer fromIndex;

    @ApiModelProperty(name = "address", value = "转入地址")
    private String address;

    @ApiModelProperty(name = "value", value = "转入金额")
    private Long value;

    public InputDto(UtxoInput input) {
        this.index = input.getIndex();
        this.fromHash = input.getFromHash().getDigestHex();
        this.fromIndex = input.getFromIndex();
        this.address = input.getFrom().getAddress();
        this.value = input.getFrom().getValue();
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getFromHash() {
        return fromHash;
    }

    public void setFromHash(String fromHash) {
        this.fromHash = fromHash;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }
}
