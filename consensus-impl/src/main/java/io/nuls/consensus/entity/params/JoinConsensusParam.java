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
package io.nuls.consensus.entity.params;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.param.AssertUtil;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class JoinConsensusParam {
    public static final String DEPOSIT = "deposit";
    public static final String AGENT_ADDRESS = "agentAddress";
    public static final String INTRODUCTION = "introduction";

    private final Map<String, Object> params;

    public JoinConsensusParam(Map<String, Object> map) {
        AssertUtil.canNotEmpty(map, ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(DEPOSIT), ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(AGENT_ADDRESS), ErrorCode.NULL_PARAMETER.getMsg());
        this.params = map;
    }

    public Boolean isSeed() {
        return false;
    }

    public Double getDeposit() {
        return (Double) params.get(DEPOSIT);
    }

    public String getAgentAddress() {
        return (String) params.get(AGENT_ADDRESS);
    }

    public String getIntroduction() {
        return (String) params.get(INTRODUCTION);
    }

}
