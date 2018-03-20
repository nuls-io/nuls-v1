package io.nuls.rpc.entity;

import io.nuls.core.utils.date.DateUtil;

public class InfoDto {

    private Long localBestHeight;

    private Long netBestHeight;

    private String timeOffset;

    private int inCount;

    private int outCount;

    public InfoDto() {

    }

    public InfoDto(long localBestHeight, long netBestHeight, long offsetTime) {
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
