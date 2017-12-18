package io.nuls.core.mesasge.constant;

/**
 * @author Niels
 * @date 2017/11/7
 */
public enum MessageTypeEnum {
    NETWORK(1), EVENT(2);
    private int code;

     MessageTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageTypeEnum getMessageTypeEnum(int code) {
        switch (code) {
            case 1:
                return NETWORK;
            case 2:
                return EVENT;
            default:
                return null;
        }
    }


}
