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
package io.nuls.network.constant;


import io.nuls.kernel.constant.ErrorCode;

/**
 * Created by Niels on 2017/9/27.
 */
public interface NetworkErrorCode {
    /*
     * ----------  Network Exception code   --------
     */
    ErrorCode NET_SERVER_START_ERROR = ErrorCode.init("NET001", "40001");
    ErrorCode NET_MESSAGE_ERROR = ErrorCode.init("NET002", "40002");
    ErrorCode NET_MESSAGE_XOR_ERROR = ErrorCode.init("NET003", "40003");
    ErrorCode NET_MESSAGE_LENGTH_ERROR = ErrorCode.init("NET004", "40004");
    ErrorCode NET_NODE_GROUP_ALREADY_EXISTS = ErrorCode.init("NET006", "40006");
    ErrorCode NET_NODE_AREA_ALREADY_EXISTS = ErrorCode.init("NET007", "40007");
    ErrorCode NET_NODE_GROUP_NOT_FOUND = ErrorCode.init("NET008", "40008");
    ErrorCode NET_NODE_AREA_NOT_FOUND = ErrorCode.init("NET009", "40009");
    ErrorCode NET_NODE_NOT_FOUND = ErrorCode.init("NET010", "40010");
    ErrorCode NET_BROADCAST_FAIL = ErrorCode.init("NET011", "40011");
    ErrorCode NET_BROADCAST_NODE_EMPTY = ErrorCode.init("NET012", "40012");
}