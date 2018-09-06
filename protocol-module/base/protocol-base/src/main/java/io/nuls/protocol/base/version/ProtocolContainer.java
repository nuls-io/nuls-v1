package io.nuls.protocol.base.version;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProtocolContainer<T extends BaseNulsData> {

    private Integer version;

    private Class<T> blockClass;

    private int percent;

    private int delay;

    private int currentDelay;

    private Set<String> addressSet;

    private int status;

    public static final int INVALID = 0;

    public static final int DELAY_LOCK = 1;

    public static final int VALID = 2;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
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

    public int getCurrentDelay() {
        return currentDelay;
    }

    public void setCurrentDelay(int currentDelay) {
        this.currentDelay = currentDelay;
    }
}
