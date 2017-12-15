package io.nuls.consensus.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Niels
 * @date 2017/12/15
 */
public class TempMainTest {

    public static void main(String[] args) {
        BigDecimal b1 = new BigDecimal(0.01);
        BigDecimal b2 = new BigDecimal(3);
        BigDecimal value = b1.divide(b2, 8, RoundingMode.UP) ;
        System.out.println(value);
    }

}
