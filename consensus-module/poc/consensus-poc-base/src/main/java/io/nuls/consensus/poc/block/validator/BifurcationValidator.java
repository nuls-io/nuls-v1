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

package io.nuls.consensus.poc.block.validator;

import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.service.BlockService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/6/8
 */
@Component
public class BifurcationValidator implements NulsDataValidator<BlockHeader> {

    private static String CLASS_NAME = BifurcationValidator.class.getName();

    @Autowired
    private BlockService blockService;

    @Autowired
    private ConsensusService consensusService;

    @Override
    public ValidateResult validate(BlockHeader header) {
        if (header.getHeight() == 0L) {
            return ValidateResult.getSuccessResult();
        }
        ValidateResult result = ValidateResult.getSuccessResult();
        if (header.getHeight() > NulsContext.getInstance().getBestHeight()) {
            return result;
        }
        BlockHeader otherBlockHeader = blockService.getBlockHeader(header.getHeight()).getData();
        if (null != otherBlockHeader && !otherBlockHeader.getHash().equals(header.getHash()) && Arrays.equals(otherBlockHeader.getPackingAddress(), header.getPackingAddress())) {
            List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getDelHeight() > 0) {
                    continue;
                }
                if (Arrays.equals(a.getPackingAddress(), header.getPackingAddress())) {
                    agent = a;
                    break;
                }
            }
            if (null == agent) {
                return ValidateResult.getFailedResult(CLASS_NAME,PocConsensusErrorCode.BIFURCATION);
            }

            RedPunishTransaction redPunishTransaction = new RedPunishTransaction();
            RedPunishData redPunishData = new RedPunishData();

            redPunishData.setAddress(agent.getAgentAddress());
            try {
                byte[] header1 = header.serialize();
                byte[] header2 = otherBlockHeader.serialize();
                redPunishData.setEvidence(ArraysTool.concatenate(header1, header2));
            } catch (Exception e) {
                return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.SYS_UNKOWN_EXCEPTION);
            }
            redPunishData.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
            redPunishTransaction.setTxData(redPunishData);
            CoinData coinData = null;
            try {
                coinData = ConsensusTool.getStopAgentCoinData(redPunishData.getAddress(), PocConsensusConstant.RED_PUNISH_LOCK_TIME);
            } catch (IOException e) {
                Log.error(e);
                return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.DATA_ERROR);
            }
            redPunishTransaction.setCoinData(coinData);
            try {
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            } catch (IOException e) {
                Log.error(e);
                return ValidateResult.getFailedResult(CLASS_NAME, PocConsensusErrorCode.BIFURCATION);
            }
            this.consensusService.newTx(redPunishTransaction);
            return ValidateResult.getFailedResult(CLASS_NAME, PocConsensusErrorCode.BIFURCATION);
        }

        return result;
    }

}