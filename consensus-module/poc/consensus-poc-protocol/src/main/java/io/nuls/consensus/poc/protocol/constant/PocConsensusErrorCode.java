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

package io.nuls.consensus.poc.protocol.constant;

import io.nuls.kernel.constant.ErrorCode;

/**
 * @author: Niels Wang
 * @date: 2018/5/13
 */
public interface PocConsensusErrorCode {
    ErrorCode CS_UNKOWN_EXCEPTION = ErrorCode.init("CS000", "70000");
    ErrorCode TIME_OUT = ErrorCode.init("CS001", "70001");
    ErrorCode DEPOSIT_ERROR = ErrorCode.init("CS002", "70002");
    ErrorCode DEPOSIT_NOT_ENOUGH = ErrorCode.init("CS003", "70003");
    ErrorCode CONSENSUS_EXCEPTION = ErrorCode.init("CS004", "70004");
    ErrorCode COMMISSION_RATE_OUT_OF_RANGE = ErrorCode.init("cs005", "70005");
    ErrorCode LACK_OF_CREDIT = ErrorCode.init("cs006", "70006");
    ErrorCode DEPOSIT_OVER_COUNT = ErrorCode.init("cs007", "70007");
    ErrorCode DEPOSIT_TOO_MUCH = ErrorCode.init("cs008", "70008");
    ErrorCode AGENT_STOPPED = ErrorCode.init("cs009","70009");

    ErrorCode DEPOSIT_WAS_CANCELED = ErrorCode.init("cs010","70010");
    ErrorCode DEPOSIT_NEVER_CANCELED = ErrorCode.init("cs011","70011");
    ErrorCode UPDATE_DEPOSIT_FAILED = ErrorCode.init("cs012","70012");
    ErrorCode SAVE_ERROR = ErrorCode.init("cs013","70013");
    ErrorCode UPDATE_AGENT_FAILED = ErrorCode.init("cs014","70014");
    ErrorCode LOCK_TIME_NOT_REACHED = ErrorCode.init("cs015","70015");

    ErrorCode AGENT_NOT_EXIST = ErrorCode.init("cs016","70016");
    ErrorCode AGENT_EXIST = ErrorCode.init("cs017","70017");
    ErrorCode AGENT_PUNISHED = ErrorCode.init("cs018","70018");
    ErrorCode BIFURCATION = ErrorCode.init("cs019","70019");
    ErrorCode YELLOW_PUNISH_TX_WRONG = ErrorCode.init("cs020","70020");
    ErrorCode ADDRESS_IS_CONSENSUS_SEED = ErrorCode.init("cs021","70021");
    ErrorCode TRANSACTIONS_NEVER_DOUBLE_SPEND = ErrorCode.init("cs022","70022");
    ErrorCode WRONG_RED_PUNISH_REASON = ErrorCode.init("cs023","70023");
    ErrorCode AGENT_PACKING_EXIST = ErrorCode.init("cs024","70024");


}
