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
package io.nuls.consensus.poc.service;

import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.model.*;
import io.nuls.consensus.poc.provider.QueueProvider;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface PocConsensusService extends ConsensusService {

    void addProvider(QueueProvider blockQueueProvider, QueueProvider txQueueProvider);

    Transaction startConsensus(String agentAddress, String password, Map<String, Object> paramsMap) throws NulsException;

    Transaction stopConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException, IOException;

    Map<String, Object> getConsensusInfo();

    Map<String, Object> getConsensusInfo(String address);

    /**
     * for client Customized
     */
    List<Consensus<Agent>> getAllAgentList();

    Consensus<Agent> getAgentByAddress(String address);

    List<Consensus<Agent>> getEffectiveAgentList(String address, long height, Integer status);

    List<Consensus<Deposit>> getAllDepositList();

    List<Consensus<Deposit>> getEffectiveDepositList(String address, String agentId, long height, Integer status);

    Page<Map<String, Object>> getAgentList(String keyword, String address, String agentAddress, String sortType, Integer pageNumber, Integer pageSize);

    Page<Map<String, Object>> getDepositList(String address, String agentAddress, Integer pageNumber, Integer pageSize);

    Map<String, Object> getAgent(String agentAddress);

    MeetingRound getCurrentRound();
}
