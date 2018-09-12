package io.nuls.protocol.base.version;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProtocolContainer<T extends BaseNulsData> {
    /**
     * 协议版本号
     */
    private Integer version;
    /**
     * 协议对应区块
     */
    private Class<T> blockClass;
    /***协议生效覆盖率*/
    private int percent;
    /***当前出块轮次*/
    private long roundIndex;
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

    /**
     * 协议生效时的区块高度
     */
    private Long effectiveHeight;
    /**
     * 协议生效状态
     */
    private int status;
    /**
     * 协议未生效
     */
    public static final int INVALID = 0;
    /**
     * 协议覆盖率已达到，处于延迟生效
     */
    public static final int DELAY_LOCK = 1;
    /**
     * 协议已生效
     */
    public static final int VALID = 2;


    public ProtocolContainer() {
        addressSet = new HashSet<>();
    }

    /**
     * 该版本所生效的Transaction协议，map的key是交易的类型
     */
    private Map<Integer, Class<? extends Transaction>> txMap = new HashMap<>();

    /**
     * 该版本所生效的Message协议，map的key是消息的moduleId + messageType的组合，实际的key字符串例如 "3-1"
     */
    private Map<String, Class<? extends BaseMessage>> messageMap = new HashMap<>();


    public void putTransaction(int type, Class<? extends Transaction> clazz) {
        this.txMap.put(type, clazz);
    }

    public void putMessage(String key, Class<? extends BaseMessage> clazz) {
        this.messageMap.put(key, clazz);
    }

    public Map<Integer, Class<? extends Transaction>> getTxMap() {
        return txMap;
    }

    public Map<String, Class<? extends BaseMessage>> getMessageMap() {
        return messageMap;
    }

    public Transaction getTransaction(NulsByteBuffer byteBuffer) {
        return null;
    }

    public BaseMessage getMessage(NulsByteBuffer byteBuffer) {
        return null;
    }

    public boolean containsTxType(int type) {
        return txMap.containsKey(type);
    }

    public boolean containsMessageType(int moduleId, int type) {
        return messageMap.containsKey(moduleId + "-" + type);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Class<T> getBlockClass() {
        return blockClass;
    }

    public void setBlockClass(Class<T> blockClass) {
        this.blockClass = blockClass;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
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

    public long getCurrentDelay() {
        return currentDelay;
    }

    public void setCurrentDelay(long currentDelay) {
        this.currentDelay = currentDelay;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
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

}
