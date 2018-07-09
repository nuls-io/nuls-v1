package io.nuls.kernel.func;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Niels
 * @date 2018/7/7
 */
public class TimeServiceTest {

    public static void main(String[] args) {
        TimeService timeService = TimeService.getInstance();

        timeService.run();

        assertNotEquals(0L,TimeService.getNetTimeOffset());
    }
}