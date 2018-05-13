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

package io.nuls.consensus.poc.protocol.validator;

import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

/**
 * @author ln
 * @date 2018/5/10
 */
@Component
public class DepositTxValidator implements NulsDataValidator<DepositTransaction> {

    @Autowired
    private PunishLogStorageService punishLogStorageService;

    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public ValidateResult validate(DepositTransaction tx) {
        if (null == tx || null == tx.getTxData() || null == tx.getTxData().getAgentHash() || null == tx.getTxData().getDeposit() || null == tx.getTxData().getAddress()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "the deposit tx is Incomplete!");
        }
        Deposit deposit = tx.getTxData();
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash());
        if (null == agentPo) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "Agent is not exist!");
        }
        long count = 0;
        try {
            count = punishLogStorageService.getCountByType(deposit.getAddress(), PunishType.RED.getCode());
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (count > 0) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.LACK_OF_CREDIT);
        }


        return ValidateResult.getSuccessResult();
    }

    private boolean isDepositOk(Na deposit, CoinData coinData) {
        return deposit.equals(coinData.getTo().get(0).getNa());
    }

}
