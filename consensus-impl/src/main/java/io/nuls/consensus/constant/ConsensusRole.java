package io.nuls.consensus.constant;

import io.nuls.core.i18n.I18nUtils;

/**
 * @author Niels
 * @date 2017/12/5
 */
public enum ConsensusRole {

    AGENT(1,69994),

    DELEGATE_NODE(2,69995),

    ENTRUSTER(3,69996);
    private final int code;
    private final int textCode;

    ConsensusRole(int code,int textCode) {
        this.code = code;
        this.textCode = textCode;
    }

    /**
     * the text to show of the role
     *
     * @return
     */
    public String getText() {
        return I18nUtils.get(textCode);
    }
    public int getCode() {
        return code;
    }

    public static ConsensusRole getConsensusRoleByCode(int code) {
        switch (code) {
            case 1:
                return AGENT;
            case 2:
                return DELEGATE_NODE;
            case 3:
                return ENTRUSTER;
            default:
                return null;
        }
    }
}
