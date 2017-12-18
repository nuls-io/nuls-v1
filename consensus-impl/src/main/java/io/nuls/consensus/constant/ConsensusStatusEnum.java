package io.nuls.consensus.constant;

import io.nuls.core.i18n.I18nUtils;

/**
 * @author Niels
 * @date 2017/11/7
 */
public enum ConsensusStatusEnum {
    NOT_IN(0, 69999),
    IN(1, 69998),
    WAITING(2, 69997);
    private final int code;
    private final int textCode;

    ConsensusStatusEnum(int code, int textCode) {
        this.code = code;
        this.textCode = textCode;
    }

    public int getCode() {
        return code;
    }

    /**
     * the text to show of the status
     *
     * @return
     */
    public String getText() {
        return I18nUtils.get(textCode);
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
