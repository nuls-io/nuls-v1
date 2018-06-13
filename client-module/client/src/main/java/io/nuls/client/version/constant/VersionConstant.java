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

package io.nuls.client.version.constant;

import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;

/**
 * @author: Niels Wang
 * @date: 2018/6/13
 */
public interface VersionConstant {

    String PUBLIC_KEY = "043f48de189fe5c01c7cd746cfdc404ab1957a287ef46fe23cb23e7ff6108f188eaaa89ce8e248854a809c187e9d207c881b126f0874183c0c81efe4293e6b1db7";

    ECKey EC_KEY = ECKey.fromPublicOnly(Hex.decode(PUBLIC_KEY));


    String ROOT_URL = "https://raw.githubusercontent.com/nuls-io/nuls-wallet-release/master/";
    String VERDION_JSON_URL = ROOT_URL + "version.json";

    /**
     * 状态：0：未开始,1：下载中,2：安装中,3：等待重启,4：失败
     */
    int UN_START = 0;
    int DOWNLOADING = 1;
    int INSTALLING = 2;
    int WAITING_RESTART = 3;
    int FAILED = 4;

}
