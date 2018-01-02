package io.nuls.consensus.constant;

import io.nuls.consensus.entity.meeting.PocMeetingRound;

/**
 * @author Niels
 * @date 2018/1/2
 */
public class ConsensusContext {

    private String localAccountAddress;
    private PocMeetingRound currentRound;
    private static ConsensusContext INSTANCE = new ConsensusContext();

    private ConsensusContext() {
    }

    public static ConsensusContext getInstance() {
        return INSTANCE;
    }

    public String getLocalAccountAddress() {
        return localAccountAddress;
    }

    public void setLocalAccountAddress(String localAccountAddress) {
        this.localAccountAddress = localAccountAddress;
    }

    public PocMeetingRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(PocMeetingRound currentRound) {
        this.currentRound = currentRound;
    }

}
