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
package io.nuls.consensus.constant;

import io.nuls.core.i18n.I18nUtils;

/**
 * @author Niels
 * @date 2017/12/5
 */
public enum ConsensusRole {

    AGENT(1,69994),

    DEPOSIT_NODE(2,69995),

    ENTRUSTER(3,69996);
    private final int code;
    private final int textCode;

    ConsensusRole(int code,int textCode) {
        this.code = code;
        this.textCode = textCode;
    }

    /**
     * the text to show of the role
     *
     * @return
     */
    public String getText() {
        return I18nUtils.get(textCode);
    }
    public int getCode() {
        return code;
    }

    public static ConsensusRole getConsensusRoleByCode(int code) {
        switch (code) {
            case 1:
                return AGENT;
            case 2:
                return DEPOSIT_NODE;
            case 3:
                return ENTRUSTER;
            default:
                return null;
        }
    }
}
