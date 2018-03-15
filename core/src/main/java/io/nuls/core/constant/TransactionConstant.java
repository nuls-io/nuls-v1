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

/**
 * @author Niels
 * @date 2017/10/30
 */
public interface TransactionConstant {

    int TX_TYPE_COIN_BASE = 1;
    int TX_TYPE_TRANSFER = 2;
    int TX_TYPE_LOCK = 3;
    int TX_TYPE_UNLOCK = 4;
    int TX_TYPE_SMALL_CHANGE = 5;
    /**
     * Account
     */
    int TX_TYPE_SET_ALIAS = 11;
    int TX_TYPE_CHANGE_ALIAS = 12;

    /**
     * CONSENSUS
     */
    int TX_TYPE_REGISTER_AGENT = 90;
    int TX_TYPE_JOIN_CONSENSUS = 91;
    int TX_TYPE_EXIT_CONSENSUS = 92;
    int TX_TYPE_YELLOW_PUNISH = 93;
    int TX_TYPE_RED_PUNISH = 94;


    byte TX_OUTPUT_UNSPEND = 0;
    byte TX_OUTPUT_LOCKED = 1;
    byte TX_OUTPUT_SPENT = 2;
    byte TX_OUTPUT_UNCONFIRM = -1;

    String TX_LIST = "TX_LIST";
}
