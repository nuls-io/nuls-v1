package io.nuls.rpc.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.ledger.entity.UtxoInput;

public class InputDto {

    private Integer index;

    private String fromHash;

    private Integer fromIndex;

    private String address;

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
