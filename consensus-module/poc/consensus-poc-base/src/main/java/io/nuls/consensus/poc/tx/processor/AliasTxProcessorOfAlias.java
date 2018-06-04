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
 *
 */

package io.nuls.consensus.poc.tx.processor;

import io.nuls.account.tx.AliasTransaction;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;
import java.util.List;

/**
 * 设置别名处理器
 *
 * @author: Charlie
 * @date: 2018/5/13
 */
@Component
public class AliasTxProcessorOfAlias implements TransactionProcessor<AliasTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public Result onRollback(AliasTransaction tx, Object secondaryData) {
        List<AgentPo> agentList = agentStorageService.getList();
        for (AgentPo po : agentList) {
            if (Arrays.equals(po.getAgentAddress(), tx.getTxData().getAddress())) {
                po.setAlias(null);
                agentStorageService.save(po);
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(AliasTransaction tx, Object secondaryData) {
        List<AgentPo> agentList = agentStorageService.getList();
        for (AgentPo po : agentList) {
            if (Arrays.equals(po.getAgentAddress(), tx.getTxData().getAddress())) {
                po.setAlias(tx.getTxData().getAlias());
                agentStorageService.save(po);
            }
        }
        return Result.getSuccess();
    }

    /**
     * 冲突检测
     * 1.检测一个acount只能设置一个别名
     * 2.检查是否多个交易设置了同样的别名
     * conflictDetect
     * 1.Detecting an acount can only set one alias.
     * 2.Check if multiple aliasTransaction have the same alias.
     *
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     */
    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }
}
