package io.nuls.ledger.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;

public interface LedgerErrorCode extends TransactionErrorCode, KernelErrorCode {

    //TODO pierre 资源文件：无效交易
    ErrorCode LEDGER_INVALID_TX = ErrorCode.init("LEDGER001", 80015);
    //TODO pierre 尝试双花
    ErrorCode LEDGER_DOUBLE_SPENT = ErrorCode.init("LEDGER002", 80016);
}
