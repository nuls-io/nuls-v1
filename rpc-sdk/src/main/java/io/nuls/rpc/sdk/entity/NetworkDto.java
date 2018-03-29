package io.nuls.rpc.sdk.entity;

import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/26
 */
public class NetworkDto {

    private Long localBestHeight;

    private Long netBestHeight;

    private String timeOffset;

    private Integer inCount;

    private Integer outCount;

    public NetworkDto(Map<String, Object> map){
        this.localBestHeight = StringUtils.parseLong(map.get("localBestHeight"));
        this.netBestHeight = StringUtils.parseLong(map.get("netBestHeight"));
        this.timeOffset = (String)map.get("timeOffset");
        this.inCount = (Integer)map.get("inCount");
        this.outCount = (Integer)map.get("outCount");
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

    public Integer getInCount() {
        return inCount;
    }

    public void setInCount(Integer inCount) {
        this.inCount = inCount;
    }

    public Integer getOutCount() {
        return outCount;
    }

    public void setOutCount(Integer outCount) {
        this.outCount = outCount;
    }
}
