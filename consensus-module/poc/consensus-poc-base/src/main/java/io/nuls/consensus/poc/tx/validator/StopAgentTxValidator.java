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

package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;

import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.script.TransactionSignature;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.*;

/**
 * @author Niels
 */
@Component
public class StopAgentTxValidator implements NulsDataValidator<StopAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Override
    public ValidateResult validate(StopAgentTransaction data) throws NulsException {
        AgentPo agentPo = agentStorageService.get(data.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.AGENT_NOT_EXIST);
        }
        TransactionSignature sig = new TransactionSignature();
        try {
            sig.parse(data.getTransactionSignature(), 0);
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getErrorCode());
        }

        if (!SignatureUtil.containsAddress(data,agentPo.getAgentAddress())) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.SIGNATURE_ERROR);
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        if (data.getCoinData().getTo() == null || data.getCoinData().getTo().isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        List<DepositPo> allDepositList = depositStorageService.getList();
        Map<NulsDigestData, DepositPo> depositMap = new HashMap<>();
        Na totalNa = agentPo.getDeposit();
        DepositPo ownDeposit = new DepositPo();
        ownDeposit.setDeposit(agentPo.getDeposit());
        ownDeposit.setAddress(agentPo.getAgentAddress());
        depositMap.put(data.getTxData().getCreateTxHash(), ownDeposit);
        for (DepositPo deposit : allDepositList) {
            if (deposit.getDelHeight() > -1L && (data.getBlockHeight() == -1L || deposit.getDelHeight() < data.getBlockHeight())) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositMap.put(deposit.getTxHash(), deposit);
            totalNa = totalNa.add(deposit.getDeposit());
        }

        Na fromTotal = Na.ZERO;
        Map<String, Na> verifyToMap = new HashMap<>();
        for (Coin coin : data.getCoinData().getFrom()) {
            if (coin.getLockTime() != -1L){
                return ValidateResult.getFailedResult(this.getClass().getName(),TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
                NulsDigestData txHash = new NulsDigestData();
            txHash.parse(coin.getOwner(), 0);
            DepositPo deposit = depositMap.remove(txHash);
            if (deposit == null) {
                return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            if (deposit.getAgentHash() == null && !coin.getNa().equals(agentPo.getDeposit())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            } else if (!deposit.getDeposit().equals(coin.getNa())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            fromTotal = fromTotal.add(coin.getNa());
            if (deposit.getAgentHash() == null) {
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
            Na na = verifyToMap.get(address);
            if (null == na) {
                na = deposit.getDeposit();
            } else {
                na = na.add(deposit.getDeposit());
            }
            verifyToMap.put(address, na);
        }
        if (!depositMap.isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (!totalNa.equals(fromTotal)) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        Na ownToCoin = ownDeposit.getDeposit().subtract(data.getFee());
        long ownLockTime = 0L;
        for (Coin coin : data.getCoinData().getTo()) {
            //String address = AddressTool.getStringAddressByBytes(coin.());
            String address = AddressTool.getStringAddressByBytes(coin.getAddress());
            Na na = verifyToMap.get(address);
            if (null != na && na.equals(coin.getNa())) {
                verifyToMap.remove(address);
                continue;
            }
            if (ownToCoin != null && Arrays.equals(coin.getAddress(), ownDeposit.getAddress()) && coin.getNa().equals(ownToCoin)) {
                ownToCoin = null;
                ownLockTime = coin.getLockTime();
                continue;
            } else {
                return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
        }
        if (ownLockTime < (data.getTime() + PocConsensusConstant.STOP_AGENT_LOCK_TIME)) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.LOCK_TIME_NOT_REACHED);
        }
        if (!verifyToMap.isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }

        return ValidateResult.getSuccessResult();
    }
}
