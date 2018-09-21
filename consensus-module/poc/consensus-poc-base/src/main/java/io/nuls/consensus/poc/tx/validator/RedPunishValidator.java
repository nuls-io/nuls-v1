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

import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.model.validator.HeaderSignValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 */
@Component
public class RedPunishValidator extends BaseConsensusProtocolValidator<RedPunishTransaction> {

    private static final String CLASS_NAME = RedPunishValidator.class.getName();

    @Autowired
    private HeaderSignValidator validator;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public ValidateResult validate(RedPunishTransaction data) {
        RedPunishData punishData = data.getTxData();
        if (ConsensusConfig.getSeedNodeStringList().contains(AddressTool.getStringAddressByBytes(punishData.getAddress()))) {
            return ValidateResult.getFailedResult(CLASS_NAME, PocConsensusErrorCode.ADDRESS_IS_CONSENSUS_SEED);
        }
        if (punishData.getReasonCode() == PunishReasonEnum.DOUBLE_SPEND.getCode()) {
            SmallBlock smallBlock = new SmallBlock();
            try {
                smallBlock.parse(punishData.getEvidence(), 0);
            } catch (NulsException e) {
                Log.error(e);
                return ValidateResult.getFailedResult(CLASS_NAME, e.getErrorCode());
            }
            BlockHeader header = smallBlock.getHeader();
            ValidateResult result = validator.validate(header);
            if (result.isFailed()) {
                return ValidateResult.getFailedResult(CLASS_NAME, result.getErrorCode());
            }
            List<NulsDigestData> txHashList = smallBlock.getTxHashList();
            if (!header.getMerkleHash().equals(NulsDigestData.calcMerkleDigestData(txHashList))) {
                return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            List<Transaction> txList = smallBlock.getSubTxList();
            if (null == txList || txList.size() < 2) {
                return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            result = ledgerService.verifyDoubleSpend(txList);
            if (result.isSuccess()) {
                return ValidateResult.getFailedResult(CLASS_NAME, PocConsensusErrorCode.TRANSACTIONS_NEVER_DOUBLE_SPEND);
            }
        }else if (punishData.getReasonCode() == PunishReasonEnum.BIFURCATION.getCode()) {
            NulsByteBuffer byteBuffer = new NulsByteBuffer(punishData.getEvidence());
            //轮次
            long[] roundIndex = new long[NulsContext.REDPUNISH_BIFURCATION];
            for (int i = 0; i < NulsContext.REDPUNISH_BIFURCATION && !byteBuffer.isFinished(); i++) {
                BlockHeader header1 = null;
                BlockHeader header2 = null;
                try {
                    header1 = byteBuffer.readNulsData(new BlockHeader());
                    header2 = byteBuffer.readNulsData(new BlockHeader());
                } catch (NulsException e) {
                    Log.error(e);
                }
                if (null == header1 || null == header2) {
                    return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.DATA_NOT_FOUND);
                }
                if (header1.getHeight() != header2.getHeight()) {
                    return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
                }
                ValidateResult result = validator.validate(header1);
                if (result.isFailed()) {
                    return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
                }
                result = validator.validate(header2);
                if (result.isFailed()) {
                    return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
                }
                if (!Arrays.equals(header1.getBlockSignature().getPublicKey(), header2.getBlockSignature().getPublicKey())) {
                    return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.SIGNATURE_ERROR);
                }

                BlockExtendsData blockExtendsData = new BlockExtendsData(header1.getExtend());
                roundIndex[i] = blockExtendsData.getRoundIndex();
            }
            //验证轮次是否连续
            boolean rs = true;
            for (int i = 0; i < roundIndex.length; i++) {
                if (i < roundIndex.length - 2 && roundIndex[i + 1] - roundIndex[i] != 1) {
                    rs = false;
                    break;
                }
            }
            if (!rs) {
                return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.WRONG_RED_PUNISH_REASON);
            }
        }

        try {
            return verifyCoinData(data);
        } catch (IOException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }
    }

    private ValidateResult verifyCoinData(RedPunishTransaction tx) throws IOException {
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent theAgent = null;
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0 && (tx.getBlockHeight() <= 0 || agent.getDelHeight() < tx.getBlockHeight())) {
                continue;
            }
            if (Arrays.equals(tx.getTxData().getAddress(), agent.getAgentAddress())) {
                theAgent = agent;
            }
        }
        if (null == theAgent) {
            return ValidateResult.getFailedResult(CLASS_NAME, PocConsensusErrorCode.AGENT_NOT_EXIST);
        }
        CoinData coinData = ConsensusTool.getStopAgentCoinData(theAgent, tx.getTime() + PocConsensusConstant.RED_PUNISH_LOCK_TIME, tx.getBlockHeight());
        if (!Arrays.equals(coinData.serialize(), tx.getCoinData().serialize())) {
            Log.error("红牌惩罚验证不通过, 红牌类型:{}, - 高度:{}, -红牌交易时间:{}", tx.getTxData().getReasonCode(), tx.getBlockHeight(), tx.getTime());
            return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.SERIALIZE_ERROR);
        }
        Log.info("++++++++++ 红牌惩罚验证器验证通过, 红牌类型:{}, - 高度:{}, -红牌交易时间:{}", tx.getTxData().getReasonCode(), tx.getBlockHeight(), tx.getTime());
        return ValidateResult.getSuccessResult();
    }
}
