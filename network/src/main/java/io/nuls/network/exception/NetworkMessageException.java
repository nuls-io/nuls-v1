package io.nuls.network.exception;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

/**
 * @author vivi
 * @date 2017/11/23.
 */
public class NetworkMessageException extends NulsRuntimeException {
    public NetworkMessageException(ErrorCode message) {
        super(message);
    }

    public NetworkMessageException(ErrorCode message, Throwable cause) {
        super(message, cause);
    }
}
