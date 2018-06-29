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
package io.nuls.sdk.constant;


/**
 * 内核模块的所有错误码常量定义
 * @author: Niels Wang
 */
public interface KernelErrorCode {

    /**
     * ----------  System Exception code   ---------
     */
    ErrorCode SUCCESS = ErrorCode.init("SYS000", "10000");
    ErrorCode FAILED = ErrorCode.init("SYS001", "10001");
    ErrorCode WARNNING = ErrorCode.init("SYS002", "10998");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("SYS999", "10999");
    ErrorCode FILE_NOT_FOUND = ErrorCode.init("SYS002", "10002");
    ErrorCode NULL_PARAMETER = ErrorCode.init("SYS003", "10003");
    ErrorCode INTF_REPETITION = ErrorCode.init("SYS004", "10004");
    ErrorCode THREAD_REPETITION = ErrorCode.init("SYS005", "10005");
    ErrorCode DATA_ERROR = ErrorCode.init("SYS006", "10006");
    ErrorCode THREAD_MODULE_CANNOT_NULL = ErrorCode.init("SYS007", "10007");
    ErrorCode INTF_NOTFOUND = ErrorCode.init("SYS008", "10008");
    ErrorCode CONFIGURATION_ITEM_DOES_NOT_EXIST = ErrorCode.init("SYS009", "10009");
    ErrorCode LANGUAGE_CANNOT_SET_NULL = ErrorCode.init("SYS010", "10010");
    ErrorCode IO_ERROR = ErrorCode.init("SYS011", "10011");
    ErrorCode PARSE_OBJECT_ERROR = ErrorCode.init("SYS012", "10012");
    ErrorCode HASH_ERROR = ErrorCode.init("SYS013", "10013");
    ErrorCode DATA_SIZE_ERROR = ErrorCode.init("SYS014", "10014");
    ErrorCode DATA_FIELD_CHECK_ERROR = ErrorCode.init("SYS015", "10015");
    ErrorCode CONFIG_ERROR = ErrorCode.init("SYS016", "10016");
    ErrorCode MODULE_LOAD_TIME_OUT = ErrorCode.init("SYS017", "10017");
    ErrorCode PARAMETER_ERROR = ErrorCode.init("SYS018", "10018");
    ErrorCode DATA_NOT_FOUND = ErrorCode.init("SYS019", "10019");
    ErrorCode FILE_BROKEN = ErrorCode.init("SYS020", "20020");
    ErrorCode SIGNATURE_ERROR = ErrorCode.init("SYS021", "20021");
    ErrorCode CHAIN_ID_ERROR = ErrorCode.init("SYS022", "20022");
    ErrorCode REQUEST_DENIED = ErrorCode.init("SYS023", "20023");
    /*
     * ----------  Consensus Network code   --------
     */
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
    /*
     * ---- direct Exception code--
     **/
    ErrorCode VERIFICATION_FAILD = ErrorCode.init("DATA004", "11000");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("DATA001", "11001");
    ErrorCode DATA_OVER_SIZE_ERROR = ErrorCode.init("DATA002", "11002");
    ErrorCode INPUT_VALUE_ERROR = ErrorCode.init("DATA003", "11003");

    /*
     * ----------  DB Exception code   --------
     */
    ErrorCode DB_MODULE_START_FAIL = ErrorCode.init("DB000", "20000");
    ErrorCode DB_UNKOWN_EXCEPTION = ErrorCode.init("DB001", "20001");
    ErrorCode DB_SESSION_MISS_INIT = ErrorCode.init("DB002", "20002");
    ErrorCode DB_SAVE_CANNOT_NULL = ErrorCode.init("DB003", "20003");
    ErrorCode DB_SAVE_BATCH_LIMIT_OVER = ErrorCode.init("DB004", "20004");
    ErrorCode DB_DATA_ERROR = ErrorCode.init("DB005", "20005");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("DB006", "20006");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("DB007", "20007");
    ErrorCode DB_ROLLBACK_ERROR = ErrorCode.init("DB008", "20008");

    /*
     * validator
     */
    ErrorCode NEW_TX_RECIEVED = ErrorCode.init("MSG001", "80001");
    ErrorCode NEW_BLOCK_HEADER_RECIEVED = ErrorCode.init("MSG002", "80002");
    ErrorCode CREATE_AN_ACCOUNT = ErrorCode.init("MSG003", "80003");
    ErrorCode CHANGE_DEFAULT_ACCOUNT = ErrorCode.init("MSG004", "80004");
    ErrorCode WALLET_PASSWORD_CHANGED = ErrorCode.init("MSG005", "80005");
    ErrorCode SET_AN_ALIAS = ErrorCode.init("MSG006", "80006");
    ErrorCode IMPORTED_AN_ACCOUNT = ErrorCode.init("MSG007", "80007");
    ErrorCode START_PACKED_BLOCK = ErrorCode.init("MSG008", "80008");
    ErrorCode REGISTER_AGENT = ErrorCode.init("MSG009", "80009");
    ErrorCode ASSEMBLED_BLOCK = ErrorCode.init("MSG010", "80010");
    ErrorCode JOIN_CONSENSUS = ErrorCode.init("MSG011", "80011");
    ErrorCode EXIT_CONSENSUS = ErrorCode.init("MSG012", "80012");
    ErrorCode CANCEL_CONSENSUS = ErrorCode.init("MSG013", "80013");
    ErrorCode BALANCE_CHANGE = ErrorCode.init("MSG014", "80014");



}