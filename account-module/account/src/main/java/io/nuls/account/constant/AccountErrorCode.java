/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.constant;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

/**
 * @author: Charlie
 */
public interface AccountErrorCode extends KernelErrorCode {

    ErrorCode PASSWORD_IS_WRONG = ErrorCode.init("50000");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("50001");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED = ErrorCode.init("50002");
    ErrorCode ACCOUNT_EXIST = ErrorCode.init("50003");
    ErrorCode ADDRESS_ERROR = ErrorCode.init("50004");
    ErrorCode ALIAS_EXIST = ErrorCode.init("50005");
    ErrorCode ALIAS_NOT_EXIST = ErrorCode.init("50006");
    ErrorCode ACCOUNT_ALREADY_SET_ALIAS = ErrorCode.init("50007");
    ErrorCode ACCOUNT_UNENCRYPTED = ErrorCode.init("50008");
    ErrorCode ALIAS_CONFLICT = ErrorCode.init("50009");
    ErrorCode HAVE_ENCRYPTED_ACCOUNT = ErrorCode.init("50010");
    ErrorCode HAVE_UNENCRYPTED_ACCOUNT = ErrorCode.init("50011");
    ErrorCode PRIVATE_KEY_WRONG = ErrorCode.init("50012");
    ErrorCode ALIAS_ROLLBACK_ERROR = ErrorCode.init("50013");
    ErrorCode ACCOUNTKEYSTORE_FILE_NOT_EXIST = ErrorCode.init("50014");
    ErrorCode ACCOUNTKEYSTORE_FILE_DAMAGED = ErrorCode.init("50015");
    ErrorCode ALIAS_FORMAT_WRONG = ErrorCode.init("50016");
    ErrorCode PASSWORD_FORMAT_WRONG = ErrorCode.init("50017");
    ErrorCode DECRYPT_ACCOUNT_ERROR = ErrorCode.init("50018");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED_AND_LOCKED = ErrorCode.init("50019");
    ErrorCode NICKNAME_TOO_LONG = ErrorCode.init("50020");
    ErrorCode INPUT_TOO_SMALL = ErrorCode.init("50021");
    ErrorCode MUST_BURN_A_NULS = ErrorCode.init("50022");
    ErrorCode SIGN_COUNT_TOO_LARGE = ErrorCode.init("50023");
    ErrorCode SIGN_ADDRESS_NOT_MATCH = ErrorCode.init("50024");
}
