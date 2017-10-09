package io.nuls.util.constant;

import io.nuls.util.cfg.I18nUtils;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public enum ErrorCode {
    /**
     * ----------  System Exception code   ---------
     */
    SUCCESS("0", 10000),
    FAILED("1", 10001),
    FILE_NOT_FOUND("2", 10002),
    NULL_PARAMETER("3", 10003),
    INTF_REPETITION("4",10004),
    THREAD_REPETITION("5",10005),
    DATA_ERROR("6",10006),
    THREAD_MODULE_CANNOT_NULL("7",10007),
    LANGUAGE_CANNOT_SET_NULL("100", 10100),
    UNKOWN("99", 99999),
    REQUEST_DENIED("400", 10400),
    CONFIGURATION_ITEM_DOES_NOT_EXIST("401", 10401),

    QUEUE_NAME_ERROR("200",10200),
    /**
     * ----------  DBException code   ---------
     */
    DB_SAVE_CANNOT_NULL("DB001", 50001),
    DB_SAVE_BATCH_LIMIT_OVER("DB002", 50002);

    private final int msg;
    private final String code;

    ErrorCode(String code, int msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return I18nUtils.get(msg);
    }

    public String getCode() {
        return code;
    }
}
