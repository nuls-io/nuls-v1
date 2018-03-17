/**
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
 */
package io.nuls.core.constant;


import io.nuls.core.i18n.I18nUtils;

/**
 * Created by Niels on 2017/9/27.
 */
public enum ErrorCode {

    /**
     * ----------  System Exception code   ---------
     */
    SUCCESS("SYS000", 10000),
    FAILED("SYS001", 10001),
    SYS_UNKOWN_EXCEPTION("SYS999", 10999),
    FILE_NOT_FOUND("SYS002", 10002),
    NULL_PARAMETER("SYS003", 10003),
    INTF_REPETITION("SYS004", 10004),
    THREAD_REPETITION("SYS005", 10005),
    DATA_ERROR("SYS006", 10006),
    THREAD_MODULE_CANNOT_NULL("SYS007", 10007),
    INTF_NOTFOUND("SYS008", 10008),
    CONFIGURATION_ITEM_DOES_NOT_EXIST("SYS009", 10009),
    LANGUAGE_CANNOT_SET_NULL("SYS010", 10010),
    IO_ERROR("SYS011", 10011),
    PARSE_OBJECT_ERROR("SYS012", 10012),
    HASH_ERROR("SYS013", 10013),
    DATA_SIZE_ERROR("SYS014", 10014),
    DATA_FIELD_CHECK_ERROR("SYS015", 10015),
    CONFIG_ERROR("SYS016", 10016),
    MODULE_LOAD_TIME_OUT("SYS017", 10017),
    PARAMETER_ERROR("SYS018", 10018),
    DATA_NOT_FOUND("SYS019", 10019),
    FILE_BROKEN("SYS020", 20020),
    /**
     * ----------  Consensus Network code   ---------
     */

    /**
     * ----------  Network Exception code   ---------
     */
    NET_SERVER_START_ERROR("NET001", 40001),
    NET_MESSAGE_ERROR("NET002", 40002),
    NET_MESSAGE_XOR_ERROR("NET003", 40003),
    NET_MESSAGE_LENGTH_ERROR("NET004", 40004),
    NET_P2P_UNKOWN_EXCEPTION("NET005", 40005),
    NET_NODE_GROUP_ALREADY_EXISTS("NET006", 40006),
    NET_NODE_AREA_ALREADY_EXISTS("NET007", 40007),
    NET_NODE_GROUP_NOT_FOUND("NET008", 40008),
    NET_NODE_AREA_NOT_FOUND("NET009", 40009),
    NET_NODE_NOT_FOUND("NET010", 40010),

    /**
     * ---- direct Exception code---
     **/
    VERIFICATION_FAILD("SYS000", 11000),
    DATA_PARSE_ERROR("DATA001", 11001),
    DATA_OVER_SIZE_ERROR("DATA002", 11002),
    INPUT_VALUE_ERROR("DATA003", 11003),
    /**
     * ----------  Account Exception code   ---------
     */
    PASSWORD_IS_WRONG("ACT000", 45000),
    ACCOUNT_NOT_EXIST("ACT001", 45001),
    ACCOUNT_IS_ALREADY_ENCRYPTED("ACT002", 45002),
    ACCOUNT_EXIST("ACT003",45003),

    /**
     * ----------  DB Exception code   ---------
     */

    DB_MODULE_START_FAIL("DB000", 20000),
    DB_UNKOWN_EXCEPTION("DB001", 20001),
    DB_SESSION_MISS_INIT("DB002", 20002),
    DB_SAVE_CANNOT_NULL("DB003", 20003),
    DB_SAVE_BATCH_LIMIT_OVER("DB004", 20004),
    DB_DATA_ERROR("DB005", 20005),
    DB_SAVE_ERROR("DB006", 20006),
    DB_UPDATE_ERROR("DB007", 20007),
    DB_ROLLBACK_ERROR("DB008", 20008),
    /**
     * ----------  MQ Exception code   ---------
     */
    QUEUE_NAME_ERROR("MQ001", 70001),

    /**
     * ----------  RPC Exception code   ---------
     */
    REQUEST_DENIED("RPC001", 50001),

    /**
     * ----------  Consensus Exception code   ---------
     */
    CS_UNKOWN_EXCEPTION("CS000", 60000),
    TIME_OUT("CS001", 60001),
    DEPOSIT_ERROR("CS002", 60002),
    DEPOSIT_NOT_ENOUGH("CS003", 60003),
    CONSENSUS_EXCEPTION("CS004", 60004),
    COMMISSION_RATE_OUT_OF_RANGE("cs005", 60005),
    LACK_OF_CREDIT("cs006", 60006),
    DELEGATE_OVER_COUNT("cs007", 60007),
    DEPOSIT_TOO_MUCH("cs008", 60008),

    /**
     * ------------  Ledger Exception code   --------------
     */
    UTXO_SPENT("LED001", 70001),
    UTXO_STATUS_CHANGE("LED002", 70002),
    BALANCE_NOT_ENOUGH("LED003", 70003),

    /**
     * messages
     */
    NEW_TX_RECIEVED("MSG001", 80001),
    NEW_BLOCK_HEADER_RECIEVED("MSG002", 80002),
    CREATE_AN_ACCOUNT("MSG003", 80003),
    CHANGE_DEFAULT_ACCOUNT("MSG004", 80004),
    WALLET_PASSWORD_CHANGED("MSG005", 80005),
    SET_AN_ALIAS("MSG006",80006 ),
    IMPORTED_AN_ACCOUNT("MSG007",80007),
    START_PACKED_BLOCK("MSG008",80008 ),
    REGISTER_AGENT("MSG009",80009),
    ASSEMBLED_BLOCK("MSG010",80010),
    JOIN_CONSENSUS("MSG011",80011 ),
    EXIT_CONSENSUS("MSG012",80012 ),
    CANCEL_CONSENSUS("MSG013",80013 ),
    BALANCE_CHANGE("MSG014",80014 );
    private final int msg;
    private final String code;

    ErrorCode(String code, int msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return I18nUtils.get(msg);
    }

    public String getCode() {
        return code;
    }
}