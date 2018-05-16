package io.nuls.network.rpc.model;

import io.nuls.core.tools.date.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "networkInfoJSON")
public class NetworkInfoDto {

    @ApiModelProperty(name = "localBestHeight", value = "本地最新区块高度")
    private Long localBestHeight;

    @ApiModelProperty(name = "netBestHeight", value = "网络最新区块高度")
    private Long netBestHeight;

    @ApiModelProperty(name = "timeOffset", value = "网络时间偏移值")
    private String timeOffset;

    @ApiModelProperty(name = "inCount", value = "被动连接节点数量")
    private int inCount;

    @ApiModelProperty(name = "outCount", value = "主动连接节点数量")
    private int outCount;

    public NetworkInfoDto() {

    }

    public NetworkInfoDto(long localBestHeight, long netBestHeight, long offsetTime) {
        this.localBestHeight = localBestHeight;
        this.netBestHeight = netBestHeight;
        this.timeOffset = DateUtil.getOffsetStringDate(offsetTime);
    }

    public Long getLocalBestHeight() {
        return localBestHeight;
    }

    public void setLocalBestHeight(Long localBestHeight) {
        this.localBestHeight = localBestHeight;
    }

    public Long getNetBestHeight() {
        return netBestHeight;
    }

    public void setNetBestHeight(Long netBestHeight) {
        this.netBestHeight = netBestHeight;
    }

    public String getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(String timeOffset) {
        this.timeOffset = timeOffset;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }
}
