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
package io.nuls.consensus.service.intf;

import io.nuls.consensus.entity.AgentInfo;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.DepositItem;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface ConsensusService {

    Na getTxFee(int txType);

    void startConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException;

    void stopConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException, IOException;

    List<Consensus> getConsensusAccountList();

    List<DepositItem> getDepositList(String address);

    List<AgentInfo> getAgentList();

    Map<String, Object> getConsensusInfo();

    Map<String, Object> getConsensusInfo(String address);

    ConsensusStatusInfo getConsensusStatus(String address);

}
