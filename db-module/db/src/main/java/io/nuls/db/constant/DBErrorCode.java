
package io.nuls.db.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

public interface DBErrorCode extends KernelErrorCode {

    ErrorCode DB_AREA_EXIST = ErrorCode.init("DB001", "20009");
    ErrorCode DB_AREA_NOT_EXIST = ErrorCode.init("DB002", "20010");

    ErrorCode DB_AREA_CREATE_EXCEED_LIMIT = ErrorCode.init("DB003", "20011");
    ErrorCode DB_AREA_CREATE_ERROR = ErrorCode.init("DB004", "20012");
    ErrorCode DB_AREA_CREATE_PATH_ERROR = ErrorCode.init("DB005", "20013");
    ErrorCode DB_AREA_DESTROY_ERROR = ErrorCode.init("DB006", "20014");
    ErrorCode DB_BATCH_CLOSE = ErrorCode.init("DB007", "20015");
}