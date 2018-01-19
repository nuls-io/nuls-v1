/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
 * SYSTEM CONSTANT
 *
 * @author Niels
 * @date 2017/9/26
 */
public interface NulsConstant {
    byte PLACE_HOLDER = (byte) 0xFF;

    String USER_CONFIG_FILE = "nuls.ini";
    String MODULES_CONFIG_FILE = "modules.ini";
    String MODULE_BOOTSTRAP_KEY = "bootstrap";

    /**
     * ----[ System] ----
     */
    String CFG_SYSTEM_SECTION = "System";
    String CFG_SYSTEM_LANGUAGE = "language";
    String CFG_SYSTEM_DEFAULT_ENCODING = "encoding";

    /**----[ Bootstrap] ----*/
//    String CFG_BOOTSTRAP_RPC_SERVER_MODULE = "RPC";
//    String CFG_BOOTSTRAP_DB_MODULE = "DB";
//    String CFG_BOOTSTRAP_QUEUE_MODULE = "MQ";
//    String CFG_BOOTSTRAP_P2P_MODULE = "P2P";
//    String CFG_BOOTSTRAP_ACCOUNT_MODULE = "ACCOUNT";
//    String CFG_BOOTSTRAP_EVENT_BUS_MODULE = "EB";
//    String CFG_BOOTSTRAP_CACHE_MODULE = "CACHE";

    /**
     * ----[ Module Id] ----
     */
    short MODULE_ID_MICROKERNEL  = 0;
    short MODULE_ID_MQ = 1;
    short MODULE_ID_DB = 2;
    short MODULE_ID_CACHE = 3;
    short MODULE_ID_NETWORK = 4;
    short MODULE_ID_ACCOUNT = 5;
    short MODULE_ID_EVENT_BUS = 6;
    short MODULE_ID_CONSENSUS = 7;
    short MODULE_ID_LEDGER = 8;
    short MODULE_ID_RPC = 9;
}
