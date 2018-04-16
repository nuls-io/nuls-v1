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
 */

package io.nuls.protocol.base.utils;

import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.entity.Consensus;

import java.util.Comparator;

/**
 * @author Niels
 * @date 2018/3/24
 */
public class AgentComparator implements Comparator<Consensus<Agent>> {

    public static final int DEPOSIT = 0;
    public static final int COMMISSION_RATE = 1;
    public static final int CREDIT_VALUE = 2;
    public static final int DEPOSITABLE = 3;

    private static final AgentComparator[] INSTANCE_ARRAY = new AgentComparator[]{
            new AgentComparator(DEPOSIT),
            new AgentComparator(COMMISSION_RATE),
            new AgentComparator(CREDIT_VALUE),
            new AgentComparator(DEPOSITABLE)
    };
    private final int sortType;

    public static AgentComparator getInstance(int sortType) {
        switch (sortType) {
            case DEPOSIT:
                return INSTANCE_ARRAY[DEPOSIT];
            case COMMISSION_RATE:
                return INSTANCE_ARRAY[COMMISSION_RATE];
            case CREDIT_VALUE:
                return INSTANCE_ARRAY[CREDIT_VALUE];
            case DEPOSITABLE:
                return INSTANCE_ARRAY[DEPOSITABLE];
        }
        return null;
    }

    private AgentComparator(int sortType) {
        this.sortType = sortType;
    }

    @Override
    public int compare(Consensus<Agent> o1, Consensus<Agent> o2) {
        switch (sortType) {
            case DEPOSIT: {
                //return (int) -(o1.getExtend().getDeposit().getValue() - o2.getExtend().getDeposit().getValue());
                if (o1.getExtend().getDeposit().getValue() < o2.getExtend().getDeposit().getValue()) {
                    return 1;
                } else if (o1.getExtend().getDeposit().getValue() == o2.getExtend().getDeposit().getValue()) {
                    return 0;
                }
                return -1;
            }
            case COMMISSION_RATE: {
                if(o1.getExtend().getCommissionRate() < o2.getExtend().getCommissionRate()) {
                    return -1;
                }else if(o1.getExtend().getCommissionRate() == o2.getExtend().getCommissionRate()) {
                    return 0;
                }
                return 1;
            }
            case CREDIT_VALUE: {
                if(o2.getExtend().getCreditVal() < o1.getExtend().getCreditVal()) {
                    return -1;
                }else if(o2.getExtend().getCreditVal() == o1.getExtend().getCreditVal()) {
                    return 0;
                }
                return 1;
            }
            case DEPOSITABLE: {
                if(o2.getExtend().getTotalDeposit() < o1.getExtend().getTotalDeposit()) {
                    return -1;
                }else if(o2.getExtend().getTotalDeposit() == o1.getExtend().getTotalDeposit()) {
                    return 0;
                }
                return 1;
            }
        }
        return 0;
    }
}
