package io.nuls.consensus.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusStatusInfo {
    private int status;
    private long startTime;
    private int parkedCount;
    private double accumulativeReward;
    private Map<String, Object> extend = new HashMap<>();
//    private String agentAddress;
//    private String delegatePeerAddress;
//    private double deposit;
//    private double weightOfRound;

    public void putExtend(String key, Object value) {
        extend.put(key, value);
    }

    public void removeExtend(String key) {
        extend.remove(key);
    }

    public Object getExtendValue(String key) {
        return extend.get(key);
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public double getAccumulativeReward() {
        return accumulativeReward;
    }

    public void setAccumulativeReward(double accumulativeReward) {
        this.accumulativeReward = accumulativeReward;
    }

    public int getParkedCount() {
        return parkedCount;
    }

    public void setParkedCount(int parkedCount) {
        this.parkedCount = parkedCount;
    }
}
