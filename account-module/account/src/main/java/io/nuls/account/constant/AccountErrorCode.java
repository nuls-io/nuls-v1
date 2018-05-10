/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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

/**
 * @author: Niels Wang
 * @date: 2018/5/5
 */
public interface AccountErrorCode {

    ErrorCode PASSWORD_IS_WRONG = ErrorCode.init("ACT000", 50000);
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("ACT001", 50001);
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED = ErrorCode.init("ACT002", 50002);
    ErrorCode ACCOUNT_EXIST = ErrorCode.init("ACT003", 50003);
    ErrorCode ADDRESS_ERROR = ErrorCode.init("ACT004", 50004);
    ErrorCode ALIAS_EXIST = ErrorCode.init("ACT005", 50005);
    ErrorCode ALIAS_ERROR = ErrorCode.init("ACT006", 50006);
    ErrorCode ACCOUNT_ALREADY_SET_ALIAS = ErrorCode.init("ACT007", 50007);
    ErrorCode NULL_PARAMETER = ErrorCode.init("ACT008", 50008);
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("ACT009", 50009);
    ErrorCode SUCCESS = ErrorCode.init("ACT010", 50010);
    ErrorCode FAILED = ErrorCode.init("ACT011", 50011);
}
