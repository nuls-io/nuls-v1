package io.nuls.contract.vm.program.impl;

import java.util.HashMap;
import java.util.Map;

public class ProgramTime {

    public static final Map<String, ProgramTime> cache = new HashMap<>(1024);

    private long num;
    private long total;
    private long average;

    public void add(long time) {
        this.num += 1;
        this.total += time;
        this.average = this.total / this.num;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAverage() {
        return average;
    }

    public void setAverage(long average) {
        this.average = average;
    }

    @Override
    public String toString() {
        return "ProgramTime{" +
                "num=" + num +
                ", total=" + total +
                ", average=" + average +
                '}';
    }

}
