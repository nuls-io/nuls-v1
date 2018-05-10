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

package io.nuls.client.rpc.constant;

/**
 * @author: Niels Wang
 * @date: 2018/5/10
 */
public interface RpcConstant {

    String PACKAGES = "io.nuls.rpc.resources.impl";
    int DEFAULT_PORT = 8001;
    String DEFAULT_IP = "127.0.0.1";


    String CFG_RPC_SECTION = "client";
    String CFG_RPC_SERVER_IP = "server.ip";
    String CFG_RPC_SERVER_PORT ="server.port" ;
    String CFG_RPC_REQUEST_WHITE_SHEET="request.white.sheet";

    String WHITE_SHEET_SPLIT = ",";

}
