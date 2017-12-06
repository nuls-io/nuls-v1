package io.nuls.core.constant;


import io.nuls.core.i18n.I18nUtils;

/**
 * Created by Niels on 2017/9/27.
 */
public enum ErrorCode {

    /**
     * ----------  System Exception code   ---------
     */
    SUCCESS("SYS000", 10000),
    FAILED("SYS001", 10001),
    SYS_UNKOWN_EXCEPTION("SYS999", 10999),
    FILE_NOT_FOUND("SYS002", 10002),
    NULL_PARAMETER("SYS003", 10003),
    INTF_REPETITION("SYS004", 10004),
    THREAD_REPETITION("SYS005", 10005),
    DATA_ERROR("SYS006", 10006),
    THREAD_MODULE_CANNOT_NULL("SYS007", 10007),
    INTF_NOTFOUND("SYS008", 10008),
    CONFIGURATION_ITEM_DOES_NOT_EXIST("SYS009", 10009),
    LANGUAGE_CANNOT_SET_NULL("SYS010", 10010),
    IO_ERROR("SYS011", 10011),
    PARSE_OBJECT_ERROR("SYS012",10012),

    /**
     * ----------  Consensus Network code   ---------
     */

    /**
     * ----------  Network code   ---------
     */
    NET_SERVER_START_ERROR("NET001", 40001),
    NET_MESSAGE_ERROR("NET002", 40002),
    NET_MESSAGE_XOR_ERROR("NET003", 40003),
    NET_MESSAGE_LENGTH_ERROR("NET004", 40004),

    /**
     * ----------  p2p Network code   ---------
     */
    P2P_UNKOWN_EXCEPTION("P2P000", 30000),
    PRER_GROUP_ALREADY_EXISTS("P2P001", 30001),
    PEER_GROUP_NOT_FOUND("P2P002", 30002),

    /**
     * ---- direct ---
     **/

    VERIFICATION_FAILD("SYS000", 11000),
    DATA_PARSE_ERROR("DATA001", 11001),
    DATA_OVER_SIZE_ERROR("DATA002", 11002),
    INPUT_VALUE_ERROR("DATA003", 11003),

    /**
     * ----------  DBException code   ---------
     */

    DB_MODULE_START_FAIL("DB000", 20000),
    DB_UNKOWN_EXCEPTION("DB010", 20001),
    DB_SESSION_MISS_INIT("DB011", 20002),
    DB_SAVE_CANNOT_NULL("DB012", 20003),
    DB_SAVE_BATCH_LIMIT_OVER("DB013", 20004),
    DB_DATA_ERROR("DB014", 20005),

    /**
     * ----------  MQ Exception code   ---------
     */
    QUEUE_NAME_ERROR("MQ001", 40001),

    /**
     * ----------  RPC Exception code   ---------
     */
    REQUEST_DENIED("RPC001", 50001),

    /**
     * ----------  Consensus Network code   ---------
     */
    CS_UNKOWN_EXCEPTION("CS000", 60000),;

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
