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
import io.nuls.kernel.constant.KernelErrorCode;

/**
 * @author: Niels Wang
 */
public interface PocConsensusErrorCode extends KernelErrorCode {

    ErrorCode TIME_OUT = ErrorCode.init("70001");
    ErrorCode DEPOSIT_ERROR = ErrorCode.init("70002");
    ErrorCode DEPOSIT_NOT_ENOUGH = ErrorCode.init("70003");
    ErrorCode CONSENSUS_EXCEPTION = ErrorCode.init("70004");
    ErrorCode COMMISSION_RATE_OUT_OF_RANGE = ErrorCode.init("70005");
    ErrorCode LACK_OF_CREDIT = ErrorCode.init("70006");
    ErrorCode DEPOSIT_OVER_COUNT = ErrorCode.init("70007");
    ErrorCode DEPOSIT_TOO_MUCH = ErrorCode.init("70008");
    ErrorCode AGENT_STOPPED = ErrorCode.init("70009");

    ErrorCode DEPOSIT_WAS_CANCELED = ErrorCode.init("70010");
    ErrorCode DEPOSIT_NEVER_CANCELED = ErrorCode.init("70011");
    ErrorCode UPDATE_DEPOSIT_FAILED = ErrorCode.init("70012");

    ErrorCode UPDATE_AGENT_FAILED = ErrorCode.init("70014");
    ErrorCode LOCK_TIME_NOT_REACHED = ErrorCode.init("70015");

    ErrorCode AGENT_NOT_EXIST = ErrorCode.init("70016");
    ErrorCode AGENT_EXIST = ErrorCode.init("70017");
    ErrorCode AGENT_PUNISHED = ErrorCode.init("70018");
    ErrorCode BIFURCATION = ErrorCode.init("70019");
    ErrorCode YELLOW_PUNISH_TX_WRONG = ErrorCode.init("70020");
    ErrorCode ADDRESS_IS_CONSENSUS_SEED = ErrorCode.init("70021");
    ErrorCode TRANSACTIONS_NEVER_DOUBLE_SPEND = ErrorCode.init("70022");
    ErrorCode RED_CARD_VERIFICATION_FAILED = ErrorCode.init("70023");
    ErrorCode AGENT_PACKING_EXIST = ErrorCode.init("70024");
    ErrorCode AGENTADDR_AND_PACKING_SAME = ErrorCode.init("70025");
    ErrorCode REWARDADDR_PACKING_SAME = ErrorCode.init("70026");


}
