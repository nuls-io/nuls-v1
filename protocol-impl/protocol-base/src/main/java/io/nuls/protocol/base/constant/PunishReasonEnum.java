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
 */
package io.nuls.protocol.base.constant;

import io.nuls.core.i18n.I18nUtils;

/**
 * @author Niels
 * @date 2017/12/28
 */
public enum PunishReasonEnum {
    /**
     * Bifurcate block chain
     */
    BIFURCATION((short) 1, 69980),

    /**
     * double spend
     */
    DOUBLE_SPEND((short) 2, 69981),

    ;
    private final short code;
    private final int msgCode;

    private PunishReasonEnum(short code, int msgCode) {
        this.code = code;
        this.msgCode = msgCode;
    }

    public String getMessage() {
        return I18nUtils.get(this.msgCode);
    }

    public short getCode() {
        return code;
    }

    public static PunishReasonEnum getEnum(int code) {
        switch (code) {
            case 1:
                return PunishReasonEnum.BIFURCATION;
            case 2:
                return PunishReasonEnum.DOUBLE_SPEND;
            default:
                return null;
        }

    }
}
