package io.nuls.protocol.storage.po;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
public class ProtocolInfoPo {
    /**
     * 协议版本号
     */
    private int version;
    /***协议生效覆盖率*/
    private int percent;
    /***延迟生效区块数*/
    private long delay;
    /**
     * 当前延迟区块数
     */
    private long currentDelay;
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

    public ProtocolInfoPo() {
        addressSet = new HashSet<>();
    }

    public ProtocolInfoPo(ProtocolTempInfoPo tempInfoPo) {
        this.version = tempInfoPo.getVersion();
        this.percent = tempInfoPo.getPercent();
        this.delay = tempInfoPo.getDelay();
        this.currentDelay = tempInfoPo.getCurrentDelay();
        this.addressSet = tempInfoPo.getAddressSet();
        this.roundIndex = tempInfoPo.getRoundIndex();
        this.status = tempInfoPo.getStatus();
        this.effectiveHeight = tempInfoPo.getEffectiveHeight();
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
}
