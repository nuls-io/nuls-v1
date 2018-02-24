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
package io.nuls.network.constant;

import io.nuls.core.mesasge.NulsMessageHeader;

/**
 * @author vivi
 * @date 2017.11.10
 */
public interface NetworkConstant {

    int MESSAGE_MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + 2<<21;

    /**
     * --------[network configs] -------
     */
    String NETWORK_PROPERTIES = "network.properties";
    String NETWORK_SECTION = "network";
    String NETWORK_TYPE = "net.type";
    String NETWORK_SERVER_PORT = "network.server.port";
    String NETWORK_EXTER_PORT = "network.external.port";
    String NETWORK_MAGIC = "network.magic";
    String NETWORK_NODE_MAX_IN = "net.node.max.in";
    String NETWORK_NODE_MAX_OUT = "net.node.max.out";

    String NETWORK_NODE_IN_GROUP = "inNodes";
    String NETWORK_NODE_OUT_GROUP = "outNodes";
    String NETWORK_NODE_CONSENSUS_GROUP = "consensus_Group";

    //network message type
    short NETWORK_GET_VERSION_EVENT = 01;
    short NETWORK_VERSION_EVENT = 02;
    short NETWORK_GET_NODE_EVENT = 03;
    short NETWORK_NODE_EVENT = 04;
}
