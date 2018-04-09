package io.nuls.consensus.constant;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public enum NotFoundType {
    BLOCK(1), TRANSACTION(2), HASHES(3);

    private final int code;

    NotFoundType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static NotFoundType getType(int code) {
        switch (code) {
            case 1:
                return BLOCK;
            case 2:
                return TRANSACTION;
            case 3:
                return HASHES;
            default:
                return null;
        }
    }
}
