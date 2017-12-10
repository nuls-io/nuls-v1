package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2017/11/7
 */
public enum ConsensusStatusEnum {
    NOT_IN(0),
    IN(1),
    WAITING(2);
    private final int code;

    private ConsensusStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ConsensusStatusEnum getConsensusStatusByCode(int code) {
        switch (code) {
            case 1:
                return IN;
            case 2:
                return WAITING;
            case 0:
                return NOT_IN;
            default:
                return null;
        }
    }
}
