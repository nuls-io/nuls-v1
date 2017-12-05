package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2017/12/5
 */
public enum ConsensusRole {

    AGENT(1),

    DELEGATE_PEER(2),

    ENTRUSTER(3);
    private final int code;

    private ConsensusRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ConsensusRole getConsensusRoleByCode(int code) {
        switch (code) {
            case 1:
                return AGENT;
            case 2:
                return DELEGATE_PEER;
            case 3:
                return ENTRUSTER;
            default:
                return null;
        }
    }
}
