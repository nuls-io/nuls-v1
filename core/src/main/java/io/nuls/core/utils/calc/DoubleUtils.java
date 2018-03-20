package io.nuls.core.utils.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author: Niels Wang
 * @date: 2018/3/20
 */
public class DoubleUtils {

    public static final  int DEFAULT_SCALE = 8;

    /**
     * 创建BigDecimal对象
     *
     * @param value
     * @return
     */
    public static BigDecimal createBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * 保留scale位小数
     *
     * @param value
     * @param scale
     * @param roundingMode
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:45:32
     */
    public static double round(double value, int scale, int roundingMode) {
        BigDecimal bd = createBigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }

    /**
     * 四舍五入保留scale位小数
     *
     * @param value
     * @param scale
     * @return
     */
    public static double round(double value, int scale) {
        return round(value, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 四舍五入保留8位小数
     *
     * @param value
     * @return
     */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * 返回保留两位小数的字符串
     *
     * @param value
     * @param hasThousands
     *            是否每千位用“,”分隔的字符串
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:48:26
     */
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

    /**
     * 返回保留scale位小数的字符串
     *
     * @param value
     * @param scale
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:48:52
     */
    public static String getRoundStr(Double value, int scale) {
        return getRoundStr(value, scale, false);
    }

    /**
     * 返回保留2位小数的字符串
     *
     * @param value
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:48:52
     */
    public static String getRoundStr(Double value) {
        return getRoundStr(value, DEFAULT_SCALE, false);
    }

    /**
     * 字符串转Double类型
     *
     * @param value
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:50:04
     */
    public static Double parseDouble(String value) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return Double.parseDouble(value.replaceAll(",", "").trim());
    }

    /**
     * 字符串转Double类型，保留两位小数
     *
     * @param value
     * @return String
     */
    public static Double parseDouble(String value, int scale) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return round(Double.parseDouble(value.replaceAll(",", "").trim()), scale);
    }

    /**
     * double 加法
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double sum(double d1, double d2) {
        return sum(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    /**
     * double 减法
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double sub(double d1, double d2) {
        return sub(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    /**
     * double 乘法
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double mul(double d1, double d2) {
        return mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    /**
     * double 乘法，结果保留scale位小数
     *
     * @param d1
     * @param d2
     * @param scale
     */
    public static double mul(double d1, double d2, int scale) {
        return round(mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    /**
     * 除法，结果保留scale位小数
     *
     * @param d1
     * @param d2
     * @param scale
     * @return
     *
     * @author Niels
     * @version 2017年5月19日 下午3:52:05
     */
    public static double div(double d1, double d2, int scale) {
        return round(div(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    /**
     * double 除法，结果保留两位小数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double div(double d1, double d2) {
        return div(d1, d2, DEFAULT_SCALE);
    }

    /**
     * BigDecimal 加法
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static BigDecimal sum(BigDecimal bd1, BigDecimal bd2) {
        return bd1.add(bd2);
    }

    /**
     * BigDecimal 减法
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static BigDecimal sub(BigDecimal bd1, BigDecimal bd2) {
        return bd1.subtract(bd2);
    }

    /**
     * BigDecimal 乘法
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static BigDecimal mul(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }

    /**
     * BigDecimal 除法
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static BigDecimal div(BigDecimal bd1, BigDecimal bd2) {
        if (bd2.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("除数不能为0！");
        }
        return bd1.divide(bd2, 12, RoundingMode.HALF_UP);
    }

    /**
     * BigDecimal 加 double
     *
     * @param bd1
     * @param d2
     * @return
     */
    public static BigDecimal sum(BigDecimal bd1, double d2) {
        return sum(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimal 减 double
     *
     * @param bd1
     * @param d2
     * @return
     */
    public static BigDecimal sub(BigDecimal bd1, double d2) {
        return sub(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimal 乘以 double
     *
     * @param bd1
     * @param d2
     * @return
     */
    public static BigDecimal mul(BigDecimal bd1, double d2) {
        return mul(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimal 除以 double
     *
     * @param bd1
     * @param d2
     * @return
     */
    public static BigDecimal div(BigDecimal bd1, double d2) {
        return div(bd1, createBigDecimal(d2));
    }

    /**
     * 求绝对值
     *
     * @param d1
     * @return double
     */
    public static double abs(double d1) {
        return Math.abs(d1);
    }

}
