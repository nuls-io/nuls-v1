package io.nuls.core.mesasge.constant;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public enum MessageTypeEnum {
    NETWORK(1), EVENT(2);
    private int code;

    private MessageTypeEnum(int code) {
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
