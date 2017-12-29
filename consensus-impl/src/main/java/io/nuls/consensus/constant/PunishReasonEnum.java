package io.nuls.consensus.constant;

import io.nuls.core.i18n.I18nUtils;

/**
 * @author Niels
 * @date 2017/12/28
 */
public enum PunishReasonEnum {
    /**
     * Bifurcate block chain
     */
    BIFURCATION((short) 1, 69980),

    /**
     * double spend
     */
    DOUBLE_SPEND((short) 2, 69981),

    ;
    private final short code;
    private final int msgCode;

    private PunishReasonEnum(short code, int msgCode) {
        this.code = code;
        this.msgCode = msgCode;
    }

    public String getMessage() {
        return I18nUtils.get(this.msgCode);
    }

    public short getCode() {
        return code;
    }

    public static PunishReasonEnum getEnum(int code) {
        switch (code) {
            case 1:
                return PunishReasonEnum.BIFURCATION;
            case 2:
                return PunishReasonEnum.DOUBLE_SPEND;
            default:
                return null;
        }

    }
}
