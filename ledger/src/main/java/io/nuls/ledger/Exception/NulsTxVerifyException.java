package io.nuls.ledger.Exception;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;

/**
 * Created by facjas on 2017/11/15.
 */
public class NulsTxVerifyException extends NulsException{

    public NulsTxVerifyException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
    }
}
