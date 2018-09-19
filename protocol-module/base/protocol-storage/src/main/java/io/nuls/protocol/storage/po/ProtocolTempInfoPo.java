package io.nuls.protocol.storage.po;

import java.util.HashSet;
import java.util.Set;

public class ProtocolTempInfoPo {

    /**协议版本号*/
    private int version;
    /***协议生效覆盖率*/
    private int percent;
    /***延迟生效区块数*/
    private long delay;
    /**
     * 当前延迟区块数
     */
    private long currentDelay;

    private int currentPercent;
    /**
     * 当前轮新协议打包出块地址
     */
    private Set<String> addressSet;
    /***当前出块轮次*/
    private long roundIndex;
    /**
     * 协议生效状态
     */
    private int status;

    /**
     * 协议生效时的区块高度
     */
    private Long effectiveHeight;

    public ProtocolTempInfoPo(){
        addressSet = new HashSet<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getCurrentDelay() {
        return currentDelay;
    }

    public void setCurrentDelay(long currentDelay) {
        this.currentDelay = currentDelay;
    }

    public Set<String> getAddressSet() {
        return addressSet;
    }

    public void setAddressSet(Set<String> addressSet) {
        this.addressSet = addressSet;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Long getEffectiveHeight() {
        return effectiveHeight;
    }

    public void setEffectiveHeight(Long effectiveHeight) {
        this.effectiveHeight = effectiveHeight;
    }

    public String getProtocolKey() {
        return version + "-" + percent + "-" + delay;
    }

    public void reset() {
        this.currentDelay = 0;
        this.roundIndex = 0;
        this.status = 0;
        this.effectiveHeight = null;
        this.addressSet.clear();
    }

    public int getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(int currentPercent) {
        this.currentPercent = currentPercent;
    }
}
