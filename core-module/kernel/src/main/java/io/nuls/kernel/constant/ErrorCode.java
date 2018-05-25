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

package io.nuls.kernel.constant;

import io.nuls.kernel.i18n.I18nUtils;

/**
 * 系统所有返回编码的管理工具，所有的常量都应该用该类实现。该类整合了国际化操作，所有需要国际化的信息都应该使用该类
 * All of the system's return code management tools, all of the constants should be implemented using this class.
 * This class integrates internationalization operations, and all information that needs internationalization should be used.
 *
 * @author: Niels Wang
 * @date: 2018/5/5
 */
public class ErrorCode {
    /**
     * 消息内容的国际化编码
     * Internationalized encoding of message content.
     */
    private int msg;

    /**
     * 返回码，用于标记唯一的结果
     * The return code is used to mark the unique result.
     */
    private String code;

    public ErrorCode() {

    }

    protected ErrorCode(String code, int msg) {
        this.code = code;
        this.msg = msg;
        if (null == code) {
            throw new RuntimeException("the errorcode code cann't be null!");
        }
    }

    /**
     * 根据系统语言设置，返回国际化编码对应的字符串
     * According to the system language Settings, return the string corresponding to the internationalization encoding.
     */
    public String getMsg() {
        return I18nUtils.get(msg);
    }

    public String getCode() {
        return code;
    }

    public static final ErrorCode init(String code, int msg) {
        return new ErrorCode(code, msg);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof ErrorCode)) {
            return false;
        }
        return code.equals(((ErrorCode) obj).getCode());
    }
}
