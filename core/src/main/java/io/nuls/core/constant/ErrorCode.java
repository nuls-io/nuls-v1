package io.nuls.core.constant;


import io.nuls.core.i18n.I18nUtils;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public enum ErrorCode {


    SUCCESS("0", 00000),
    /**
     * ----------  System Exception code   ---------
     */
    SYS_UNKOWN_EXCEPTION("SYS000",10000),
    FAILED("1", 10001),
    FILE_NOT_FOUND("2", 10002),
    NULL_PARAMETER("3", 10003),
    INTF_REPETITION("4",10004),
    THREAD_REPETITION("5",10005),
    DATA_ERROR("6",10006),
    THREAD_MODULE_CANNOT_NULL("7",10007),
    INTF_NOTFOUND("8",10008),
    LANGUAGE_CANNOT_SET_NULL("100", 10100),
    UNKOWN("99", 99999),
    REQUEST_DENIED("400", 10400),
    CONFIGURATION_ITEM_DOES_NOT_EXIST("401", 10401),

    QUEUE_NAME_ERROR("200",10200),

    /**
     * ----------  Consensus Network code   ---------
     */
    CS_UNKOWN_EXCEPTION("CS000",30000),

    /**
     * ----------  p2p Network code   ---------
     */
    P2P_UNKOWN_EXCEPTION("P2P000",40000),
    P2P_GROUP_ALREADY_EXISTS("P2P001",40001),

    /**
     * ----------  DBException code   ---------
     */
    DB_SQLSESSION_INIT_FAIL("DB000",50000),
    DB_UNKOWN_EXCEPTION("DB010",50010),
    DB_SAVE_CANNOT_NULL("DB011", 50011),
    DB_SAVE_BATCH_LIMIT_OVER("DB012", 50012);

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
