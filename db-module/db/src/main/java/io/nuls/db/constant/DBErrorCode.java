
package io.nuls.db.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

public interface DBErrorCode extends KernelErrorCode {

    ErrorCode DB_MODULE_START_FAIL = ErrorCode.init("20000");
    ErrorCode DB_UNKOWN_EXCEPTION = ErrorCode.init("20001");
    ErrorCode DB_SESSION_MISS_INIT = ErrorCode.init("20002");
    ErrorCode DB_SAVE_CANNOT_NULL = ErrorCode.init("20003");
    ErrorCode DB_SAVE_BATCH_LIMIT_OVER = ErrorCode.init("20004");
    ErrorCode DB_DATA_ERROR = ErrorCode.init("20005");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("20006");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("20007");
    ErrorCode DB_ROLLBACK_ERROR = ErrorCode.init("20008");
    ErrorCode DB_AREA_EXIST = ErrorCode.init("20009");
    ErrorCode DB_AREA_NOT_EXIST = ErrorCode.init("20010");
    ErrorCode DB_AREA_CREATE_EXCEED_LIMIT = ErrorCode.init("20011");
    ErrorCode DB_AREA_CREATE_ERROR = ErrorCode.init("20012");
    ErrorCode DB_AREA_CREATE_PATH_ERROR = ErrorCode.init("20013");
    ErrorCode DB_AREA_DESTROY_ERROR = ErrorCode.init("20014");
    ErrorCode DB_BATCH_CLOSE = ErrorCode.init("20015");
}