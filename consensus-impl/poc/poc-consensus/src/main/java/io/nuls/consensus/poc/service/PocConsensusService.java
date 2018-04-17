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

import io.nuls.consensus.poc.provider.QueueProvider;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.poc.service.intf.ConsensusService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface PocConsensusService extends ConsensusService {

    void addProvider(QueueProvider blockQueueProvider, QueueProvider txQueueProvider);

    //todo 更改参数类型
    Transaction startConsensus(String agentAddress, String password, Map<String, Object> paramsMap) throws NulsException;

    //todo 更改参数类型
    Transaction stopConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException, IOException;

    //todo 返回值类型
    Map<String, Object> getConsensusInfo();

    //todo 返回值类型
    Map<String, Object> getConsensusInfo(String address);

    /**
     * for client Customized
     */
    //todo 返回值类型
    Page<Map<String, Object>> getAgentList(String keyword, String address, String agentAddress, String sortType, Integer pageNumber, Integer pageSize);

    //todo 返回值类型
    Page<Map<String, Object>> getDepositList(String address, String agentAddress, Integer pageNumber, Integer pageSize);

    //todo 返回值类型
    Map<String, Object> getAgent(String agentAddress);
}
