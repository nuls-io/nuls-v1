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
package io.nuls.core.chain.entity;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Nuls unit
 *
 * @author Niels
 */
public final class Na implements Comparable<Na>, Serializable {

    private static final long serialVersionUID = 6978149202334427537L;

    public static final int SMALLEST_UNIT_EXPONENT = 8;

    public static final long NA_VALUE = (long) Math.pow(10, SMALLEST_UNIT_EXPONENT);

    public static final long TOTAL_VALUE = 100000000L;
    public static final long MAX_NA_VALUE = TOTAL_VALUE*((long) Math.pow(10, SMALLEST_UNIT_EXPONENT));

    /**
     * Total amount of token
     */
    public static final Na MAX = Na.valueOf(TOTAL_VALUE).multiply(NA_VALUE);

    /**
     * 0 Nuls
     */
    public static final Na ZERO = Na.valueOf(0);

    /**
     * 1 Nuls
     */
    public static final Na NA = Na.valueOf(NA_VALUE);

    /**
     * 0.01 Nuls
     */
    public static final Na CENT = NA.divide(100);

    /**
     * 0.001 Nuls
     */
    public static final Na MILLICOIN = NA.divide(1000);

    /**
     * 0.000001 Nuls
     */
    public static final Na MICROCOIN = MILLICOIN.divide(1000);

    /**
     * amount
     */
    private final long value;

    private Na(final long na) {
        this.value = na;
    }

    public static Na valueOf(final long na) {
        if (MAX_NA_VALUE < na) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return new Na(na);
    }

    public int smallestUnitExponent() {
        return SMALLEST_UNIT_EXPONENT;
    }

    public long getValue() {
        return value;
    }

    public static Na parseNuls(final String str) {
        try {
            long value = new BigDecimal(str).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue();
            return Na.valueOf(value);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Na parseNuls(final double nuls) {
        try {
            long value = new BigDecimal(nuls).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue();
            return Na.valueOf(value);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public double toDouble() {
        return new BigDecimal(this.value).movePointLeft(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().doubleValue();
    }

    public Na add(final Na value) {
        return new Na(this.value + value.value);
    }

    /**
     * Alias for add
     */
    public Na plus(final Na value) {
        return add(value);
    }

    public Na subtract(final Na value) {
        return new Na(this.value - value.value);
    }

    /**
     * Alias for subtract
     */
    public Na minus(final Na value) {
        return subtract(value);
    }

    public Na multiply(final long factor) {
        return new Na(this.value * factor);
    }

    /**
     * Alias for multiply
     */
    public Na times(final long factor) {
        return multiply(factor);
    }

    /**
     * Alias for multiply
     */
    public Na times(final int factor) {
        return multiply(factor);
    }

    public Na divide(final long divisor) {
        return new Na(this.value / divisor);
    }

    /**
     * Alias for divide
     */
    public Na div(final long divisor) {
        return divide(divisor);
    }

    /**
     * Alias for divide
     */
    public Na div(final int divisor) {
        return divide(divisor);
    }

    public Na[] divideAndRemainder(final long divisor) {
        return new Na[]{new Na(this.value / divisor), new Na(this.value % divisor)};
    }

    public long divide(final Na divisor) {
        return this.value / divisor.value;
    }

    /**
     * Returns true if and only if this instance represents a monetary value greater than zero,
     * otherwise false.
     */
    public boolean isPositive() {
        return signum() == 1;
    }

    /**
     * Returns true if and only if this instance represents a monetary value less than zero,
     * otherwise false.
     */
    public boolean isNegative() {
        return signum() == -1;
    }

    /**
     * Returns true if and only if this instance represents zero monetary value,
     * otherwise false.
     */
    public boolean isZero() {
        return signum() == 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is greater than that
     * of the given other Na, otherwise false.
     */
    public boolean isGreaterThan(Na other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is less than that
     * of the given other Na, otherwise false.
     */
    public boolean isLessThan(Na other) {
        return compareTo(other) < 0;
    }

    public Na shiftLeft(final int n) {
        return new Na(this.value << n);
    }

    public Na shiftRight(final int n) {
        return new Na(this.value >> n);
    }

    public int signum() {
        if (this.value == 0) {
            return 0;
        }
        return this.value < 0 ? -1 : 1;
    }

    public Na negate() {
        return new Na(-this.value);
    }

    public String toText() {

        BigDecimal amount = new BigDecimal(value).divide(BigDecimal.valueOf(Na.NA.value));
        return amount.toPlainString();
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.value == ((Na) o).value;
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }

    @Override
    public int compareTo(final Na other) {
        return Long.compare(this.value, other.value);
    }
}
