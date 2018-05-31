package io.nuls.ledger.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;

public interface LedgerErrorCode extends TransactionErrorCode, KernelErrorCode {

    ErrorCode LEDGER_DOUBLE_SPENT = ErrorCode.init("LEDGER002", "69981");
}
