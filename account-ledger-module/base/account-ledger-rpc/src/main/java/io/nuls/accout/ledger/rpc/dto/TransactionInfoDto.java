package io.nuls.accout.ledger.rpc.dto;

import io.nuls.account.ledger.model.TransactionInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "transactionInfoJson")
public class TransactionInfoDto implements Serializable{

    @ApiModelProperty(name = "txHash", value = "交易hash")
    private String txHash;
    @ApiModelProperty(name = "blockHeight", value = "区块高度")
    private long blockHeight;
    @ApiModelProperty(name = "time", value = "创建时间")
    private long time;
    @ApiModelProperty(name = "txType", value = "交易类型")
    private int txType;
    @ApiModelProperty(name = "status", value = "状态")
    private byte status;

    public TransactionInfoDto() {

    }

    public TransactionInfoDto(TransactionInfo info) {
        this.txHash = info.getTxHash().getDigestHex();
        this.blockHeight = info.getBlockHeight();
        this.time = info.getTime();
        this.status = info.getStatus();
        this.txType = info.getTxType();
    }
}
