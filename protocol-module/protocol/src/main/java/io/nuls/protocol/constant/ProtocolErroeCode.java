package io.nuls.protocol.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

/**
 * @author: Charlie
 * @date: 2018/8/9
 */
public interface ProtocolErroeCode extends KernelErrorCode {

    ErrorCode BLOCK_HEADER_SIGN_CHECK_FAILED= ErrorCode.init("30001");
    ErrorCode BLOCK_HEADER_FIELD_CHECK_FAILED= ErrorCode.init("30002");
    ErrorCode BLOCK_FIELD_CHECK_FAILED= ErrorCode.init("30003");
    ErrorCode BLOCK_TOO_BIG= ErrorCode.init("30004");
    ErrorCode MERKLE_HASH_WRONG= ErrorCode.init("30005");

}
