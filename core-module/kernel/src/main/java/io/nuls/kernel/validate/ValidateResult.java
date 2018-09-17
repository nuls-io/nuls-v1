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
package io.nuls.kernel.validate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.model.Result;

/**
 * @author Niels
 */
public class ValidateResult<T> extends Result<T> {

    private SeverityLevelEnum level;

    private String className;

    public static ValidateResult getSuccessResult() {
        ValidateResult result = new ValidateResult();
        result.setSuccess(true);
        result.setMsg("");
        return result;
    }

    public static ValidateResult getFailedResult(String className, ErrorCode msg) {
        return getFailedResult(className, SeverityLevelEnum.WRONG, msg);
    }

    public static ValidateResult getFailedResult(String className, SeverityLevelEnum level, ErrorCode errorCode) {
        ValidateResult result = new ValidateResult();
        result.setSuccess(false);
        result.setLevel(level);
        result.setErrorCode(errorCode);
        result.setClassName(className);
        return result;
    }

    @JsonIgnore
    public SeverityLevelEnum getLevel() {
        return level;
    }

    public void setLevel(SeverityLevelEnum level) {
        this.level = level;
    }


    @JsonIgnore
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
