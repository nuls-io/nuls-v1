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

package io.nuls.sdk.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author: Niels Wang
 */
public class DoubleUtils {

    public static final int DEFAULT_SCALE = 8;

    public static BigDecimal createBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    public static double round(double value, int scale, int roundingMode) {
        BigDecimal bd = createBigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }

    public static double round(double value, int scale) {
        return round(value, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    public static String getRoundStr(Double value, int scale, boolean hasThousands) {
        if (null == value) {
            return "";
        }
        String suffix = "";
        for (int i = 0; i < scale; i++) {
            if (i == 0) {
                suffix += ".";
            }
            suffix += "0";
        }

        if (hasThousands) {
            return new DecimalFormat("###,##0" + suffix).format(round(value, scale));
        } else {
            return new DecimalFormat("##0" + suffix).format(round(value, scale));
        }
    }

    public static String getRoundStr(Double value, int scale) {
        return getRoundStr(value, scale, false);
    }

    public static String getRoundStr(Double value) {
        return getRoundStr(value, DEFAULT_SCALE, false);
    }

    public static Double parseDouble(String value) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return Double.parseDouble(value.replaceAll(",", "").trim());
    }

    public static Double parseDouble(String value, int scale) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return round(Double.parseDouble(value.replaceAll(",", "").trim()), scale);
    }

    public static double sum(double d1, double d2) {
        return round(sum(createBigDecimal(d1), createBigDecimal(d2)).doubleValue());
    }

    public static double sub(double d1, double d2) {
        return round(sub(createBigDecimal(d1), createBigDecimal(d2)).doubleValue());
    }

    public static double mul(double d1, double d2) {
        return mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    public static double mul(double d1, double d2, int scale) {
        return round(mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    public static double div(double d1, double d2, int scale) {
        return round(div(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    public static double div(double d1, double d2) {
        return div(d1, d2, DEFAULT_SCALE);
    }

    public static BigDecimal sum(BigDecimal bd1, BigDecimal bd2) {
        return bd1.add(bd2);
    }

    public static BigDecimal sub(BigDecimal bd1, BigDecimal bd2) {
        return bd1.subtract(bd2);
    }

    public static BigDecimal mul(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }

    public static BigDecimal div(BigDecimal bd1, BigDecimal bd2) {
        if (bd2.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("除数不能为0！");
        }
        return bd1.divide(bd2, 12, RoundingMode.HALF_UP);
    }

    public static BigDecimal sum(BigDecimal bd1, double d2) {
        return sum(bd1, createBigDecimal(d2));
    }

    public static BigDecimal sub(BigDecimal bd1, double d2) {
        return sub(bd1, createBigDecimal(d2));
    }

    public static BigDecimal mul(BigDecimal bd1, double d2) {
        return mul(bd1, createBigDecimal(d2));
    }

    public static BigDecimal div(BigDecimal bd1, double d2) {
        return div(bd1, createBigDecimal(d2));
    }

    public static double abs(double d1) {
        return Math.abs(d1);
    }

    public static long longValue(double val) {
        return createBigDecimal(val).longValue();
    }
}
