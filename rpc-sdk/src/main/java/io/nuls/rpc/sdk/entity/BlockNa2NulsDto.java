package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class BlockNa2NulsDto extends BlockDto {

    private List<TransactionDto> txList;

    public BlockNa2NulsDto(Map<String, Object> map, boolean all) {
        super(map, false);
        if (all) {
            this.txList = new ArrayList<>();
            for (Map<String, Object> tx : (List<Map<String, Object>>) map.get("txList")) {
                this.txList.add(new TransactionNa2NulsDto(tx));
            }
        }
    }

    @JsonProperty("reward")
    public String getReward0() {
        return Na.valueOf(getReward()).toText();
    }

    @JsonProperty("fee")
    public String getFee0() {
        return Na.valueOf(getFee()).toText();
    }

    @Override
    public List<TransactionDto> getTxList() {
        return txList;
    }

    @Override
    public void setTxList(List<TransactionDto> txList) {
        this.txList = txList;
    }
}
