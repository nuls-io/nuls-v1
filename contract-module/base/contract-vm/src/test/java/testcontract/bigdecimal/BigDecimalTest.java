/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package testcontract.bigdecimal;

import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/10/17
 */
public class BigDecimalTest implements Contract {

    @View
    public String cal(String init, String aaa, String bbb, String ccc, long longd, double doublee ) {
        StringBuilder sb = new StringBuilder("result: ");
        BigDecimal decimal = new BigDecimal(init);
        sb.append("\ndecimal: " + decimal.toPlainString());

        BigDecimal add = decimal.add(new BigDecimal(bbb));
        sb.append("\nadd: " + add.toPlainString());

        BigDecimal subtract = add.subtract(new BigDecimal(ccc));
        sb.append("\nsubtract: " + subtract.toPlainString());

        BigDecimal multiply = subtract.multiply(new BigDecimal(ccc));
        sb.append("\nmultiply: " + multiply.toPlainString());

        BigDecimal divide = multiply.divide(BigDecimal.valueOf(longd), 20, BigDecimal.ROUND_HALF_UP);
        sb.append("\ndivide: " + divide.toPlainString());

        BigDecimal multiply1 = divide.multiply(BigDecimal.valueOf(doublee));
        sb.append("\nmultiply1: " + multiply1.toPlainString());

        BigDecimal multiply2 = BigDecimal.valueOf(multiply1.doubleValue()).multiply(new BigDecimal(BigInteger.TEN));
        sb.append("\nmultiply2: " + multiply2.toPlainString());

        return sb.toString();
    }

    @View
    public String cal1() {
        /**
         * 构造函数测试
         */
        BigDecimal _int = new BigDecimal(19);
        BigDecimal _double = new BigDecimal(4.56);
        BigDecimal _long = new BigDecimal(5634345L);
        BigDecimal _String = new BigDecimal("1000022");

        /**
         * 基本方法
         */
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal fee = BigDecimal.ONE;

        BigDecimal add = amount.add(fee);
        BigDecimal subtract = amount.subtract(fee);
        BigDecimal multiply = amount.multiply(fee);

        BigDecimal divisor = new BigDecimal("4.5678");
        /**
         * 保留3位小数，且四舍五入
         * ROUND_CEILING  向正无穷方向舍入
         * ROUND_DOWN   向零方向舍入
         * ROUND_FLOOR   向负无穷方向舍入
         * ROUND_HALF_DOWN  向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，向下舍入, 例如1.55 保留一位小数结果为1.5
         * ROUND_HALF_EVEN  向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，如果保留位数是奇数，使用ROUND_HALF_UP，如果是偶数，使用ROUND_HALF_DOWN
         * ROUND_HALF_UP    向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，向上舍入, 1.55保留一位小数结果为1.6
         * ROUND_UNNECESSARY  计算结果是精确的，不需要舍入模式
         * ROUND_UP 向远离0的方向舍入
         */
        BigDecimal result = amount.divide(divisor, 3, BigDecimal.ROUND_HALF_UP);

        /**
         * 普通方法
         */
        String _toString = amount.toString();
        Double _doubleValue = amount.doubleValue();
        Float _floatValue = amount.floatValue();
        Long _longValue = amount.longValue();
        Integer _intValue = amount.intValue();

        StringBuilder sb = new StringBuilder();
        /**
         * 科学计数法问题
         */
        BigDecimal hex = new BigDecimal("192320012000000000000000");
        hex.byteValueExact();
        hex.plus();
        BigDecimal pow = hex.pow(6);

        sb.append("\npow: " + pow.toString() + " ==== " + pow.toPlainString());
        String hex2 = hex.toString();
        String plain = hex.toPlainString();

        sb.append("\nmovePointRight9: " + pow.movePointRight(9));
        sb.append("\nmovePointLeft9: " + pow.movePointLeft(9));
        sb.append("\nsetScale3: " + divisor.setScale(3));
        sb.append("\nsetScale3_ROUND_HALF_DOWN: " + divisor.setScale(3, BigDecimal.ROUND_HALF_DOWN));
        sb.append("\nsetScale3_ROUND_HALF_DOWN: " + divisor);


        return sb.toString();
    }

    @View
    public String roundModeTest() {
        BigDecimal a = new BigDecimal("209898010.866");
        BigDecimal b = new BigDecimal("3");
        BigDecimal c = a.divide(b, 2, BigDecimal.ROUND_CEILING);
        BigDecimal c1 = a.divide(b, 0, BigDecimal.ROUND_DOWN);
        BigDecimal c2 = a.divide(b, 0, BigDecimal.ROUND_FLOOR);
        BigDecimal c3 = a.divide(b, 0, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal c4 = a.divide(b, 0, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal c5 = a.divide(b, 0, BigDecimal.ROUND_HALF_UP);
        BigDecimal c7 = a.divide(b, 2, BigDecimal.ROUND_UP);
        StringBuilder sb = new StringBuilder();
        sb.append(c.toPlainString()).append("\n");
        sb.append(c1.toPlainString()).append("\n");
        sb.append(c2.toPlainString()).append("\n");
        sb.append(c3.toPlainString()).append("\n");
        sb.append(c4.toPlainString()).append("\n");
        sb.append(c5.toPlainString()).append("\n");
        sb.append(c7.toPlainString()).append("\n");
        return sb.toString();
    }

}
