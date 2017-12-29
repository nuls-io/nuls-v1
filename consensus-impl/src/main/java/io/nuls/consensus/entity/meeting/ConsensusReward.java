package io.nuls.consensus.entity.meeting;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 * @date 2017/12/29
 */
public class ConsensusReward {

    private String address;
    private Na reward;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Na getReward() {
        return reward;
    }

    public void setReward(Na reward) {
        this.reward = reward;
    }
}
