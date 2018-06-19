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

package io.nuls.account.constant;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.model.Na;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public interface AccountConstant extends NulsConstant {

    /**
     * The module id of the message-bus module
     */
   short MODULE_ID_ACCOUNT = 5;

    /**
     * The name of accouts cache
     */
    String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";


    Na ALIAS_NA = Na.parseNuls(1);



    /**
     * 设置账户别名的交易类型
     * Set the transaction type of account alias.
     */
    int TX_TYPE_ACCOUNT_ALIAS = 3;

    /**
     * 账户解锁时间最大值(单位:秒)
     * Account unlock time maximum (unit: second)
     */
    int ACCOUNT_MAX_UNLOCK_TIME = 120;

    /**
     * 导出accountkeystore文件的后缀名
     * The suffix of the accountkeystore file
     */
    String ACCOUNTKEYSTORE_FILE_SUFFIX=".keystore";
}
