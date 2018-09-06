/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.kernel.constant;

/**
 * Created by ln on 2018/5/6.
 */
public interface TransactionErrorCode extends KernelErrorCode {

    ErrorCode UTXO_UNUSABLE = ErrorCode.init("31001");
    ErrorCode UTXO_STATUS_CHANGE = ErrorCode.init("31002");
    ErrorCode INVALID_INPUT = ErrorCode.init("31004");
    ErrorCode INVALID_AMOUNT = ErrorCode.init("31005");
    ErrorCode ORPHAN_TX = ErrorCode.init("31006");
    ErrorCode ORPHAN_BLOCK = ErrorCode.init("31007");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("31008");
    ErrorCode FEE_NOT_RIGHT = ErrorCode.init("31009");
    ErrorCode ROLLBACK_TRANSACTION_FAILED = ErrorCode.init("31010");
    ErrorCode TRANSACTION_REPEATED = ErrorCode.init("31011");
    ErrorCode TOO_SMALL_AMOUNT = ErrorCode.init("31012");
    ErrorCode TX_SIZE_TOO_BIG = ErrorCode.init("31013");
    ErrorCode SAVE_TX_ERROR = ErrorCode.init("31014");
    ErrorCode TX_NOT_EXIST = ErrorCode.init("31015");
    ErrorCode COINDATA_NOT_FOUND = ErrorCode.init("31016");
    ErrorCode TX_TYPE_ERROR = ErrorCode.init("31017");
}