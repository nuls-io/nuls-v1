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

package io.nuls.network.constant;

public interface NetworkConstant {

    short NETWORK_MODULE_ID = 4;


    /**
     * -----------[netty configs ]------------
     */
    int READ_IDEL_TIME_OUT = 10;
    int WRITE_IDEL_TIME_OUT = 0;
    int ALL_IDEL_TIME_OUT = 0;
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    int CONNETCI_TIME_OUT = 6000;
    int SAME_IP_MAX_COUNT = 10;
    int CONNECT_FAIL_MAX_COUNT = 6;

    /**
     * --------[network configs] -------
     */
    String NETWORK_SECTION = "network";
    String NETWORK_SERVER_PORT = "network.server.port";
    String NETWORK_MAGIC = "network.magic";
    String NETWORK_NODE_MAX_IN = "network.max.in";
    String NETWORK_NODE_MAX_OUT = "network.max.out";
    String NETWORK_SEED_IP = "network.seed.ip";
    String NETWORK_NODE_IN_GROUP = "inGroup";
    String NETWORK_NODE_OUT_GROUP = "outGroup";
    String CACHE_P2P_NODE = "cacheNode";
    String CACHE_P2P_IP = "cacheIP";
    String NODE_FILE_NAME = ".nodes";

    int HANDSHAKE_SEVER_TYPE = 2;
    int HANDSHAKE_CLIENT_TYPE = 1;

    //network message type
    short NETWORK_GET_VERSION = 1;
    short NETWORK_VERSION = 2;
    short NETWORK_GET_NODE = 3;
    short NETWORK_NODE = 4;
    short NETWORK_GET_NODEIP = 5;
    short NETWORK_NODEIP = 6;
    short NETWORK_HANDSHAKE = 7;
    short NETWORK_P2P_NODE = 8;

}
