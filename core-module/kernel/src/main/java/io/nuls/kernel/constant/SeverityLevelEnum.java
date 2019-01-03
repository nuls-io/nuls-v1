/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

/**
 * 验证器验证不通过时，需要判断错误的严重程度，目前将验证结果的严重级别定义在该枚举类中
 * <p>
 * The validator validates the out-of-date and needs to determine the severity of the error,
 * and the severity of the result is currently defined in the enumeration class.
 *
 * @author Niels
 */
public enum SeverityLevelEnum {

    /**
     * 正常范围内的错误，可能是字段不全，时间不对等等不需要进行惩罚的级别
     * Errors in the normal range, such as incomplete fields, incorrect time, etc., do not require penalties.
     */
    WRONG,

    /**
     * 犯规，但不构成惩罚
     * A foul, but not a punishment.
     */
    NORMAL_FOUL,

    /**
     * 恶意犯规，需要进行惩罚
     * A flagrant foul must be punished.
     */
    FLAGRANT_FOUL;
}
