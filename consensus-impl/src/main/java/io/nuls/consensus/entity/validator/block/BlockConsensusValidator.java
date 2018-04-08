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
package io.nuls.consensus.entity.validator.block;

import io.nuls.account.entity.Address;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.manager.RoundManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class BlockConsensusValidator implements NulsDataValidator<Block> {
    private static BlockConsensusValidator INSTANCE = new BlockConsensusValidator();

    private RoundManager roundManager = RoundManager.getInstance();
    private BlockService blockService;


    private BlockConsensusValidator() {
    }

    public static BlockConsensusValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        if (block.getHeader().getHeight() == 0) {
            return ValidateResult.getSuccessResult();
        }
        BlockRoundData roundData = new BlockRoundData(block.getHeader().getExtend());
        BlockRoundData preBlockRoundData;
        String preHash = block.getHeader().getPreHash().getDigestHex();
        while (true) {
            Block preBlock = getBlockService().getBlock(preHash);
            if (null == preBlock) {
                return ValidateResult.getFailedResult(ErrorCode.ORPHAN_BLOCK, "pre block not exist!");
            }
            preHash = preBlock.getHeader().getPreHash().getDigestHex();
            preBlockRoundData = new BlockRoundData(preBlock.getHeader().getExtend());
            if (preBlockRoundData.getRoundIndex() > roundData.getRoundIndex()) {
                return ValidateResult.getFailedResult("block round calc wrong!");
            }
            if (preBlockRoundData.getRoundIndex() < roundData.getRoundIndex()) {
                break;
            }
        }
        PocMeetingRound round = roundManager.getRound(preBlockRoundData.getRoundIndex(), roundData.getRoundIndex(), true);
        if (null == round) {
            return ValidateResult.getFailedResult(ErrorCode.ORPHAN_BLOCK, "round is null");
        }
        Block preBlock = getBlockService().getBlock(block.getHeader().getPreHash().getDigestHex());
        if (null == preBlock) {
            return ValidateResult.getFailedResult(ErrorCode.ORPHAN_BLOCK);
        }

//        ValidateResult result = this.checkCoinBaseTx(block.getTxs(), roundData, round);
//        if (result.isFailed()) {
//            return result;
//        }
        PocMeetingMember member = round.getMember(roundData.getPackingIndexOfRound());
        if (member == null || !member.getPackingAddress().equals(Address.fromHashs(block.getHeader().getPackingAddress()).getBase58())) {
            return ValidateResult.getFailedResult("round index is not inconsistent!");
        }
        ValidateResult result = this.checkYellowPunishTx(block, roundData, preBlock, member, round);
        return result;

    }

    private ValidateResult checkYellowPunishTx(Block newBlock, BlockRoundData roundData, Block preBlock, PocMeetingMember member, PocMeetingRound round) {
        YellowPunishTransaction yellowPunishTx = null;
        for (Transaction tx : newBlock.getTxs()) {
            if (tx.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH) {
                if (yellowPunishTx == null) {
                    yellowPunishTx = (YellowPunishTransaction) tx;
                } else {
                    return ValidateResult.getFailedResult("There are too many yellow punish transactions!");
                }
            }
        }
        BlockRoundData preRoundData = new BlockRoundData(preBlock.getHeader().getExtend());
        //when the blocks are continuous
        boolean isContinuous = preRoundData.getRoundIndex() == roundData.getRoundIndex() && preRoundData.getPackingIndexOfRound() == (roundData.getPackingIndexOfRound() - 1);
        isContinuous = isContinuous || (preRoundData.getRoundIndex() == (roundData.getRoundIndex() - 1) && preRoundData.getPackingIndexOfRound() == preRoundData.getConsensusMemberCount() &&
                roundData.getPackingIndexOfRound() == 1);
        //Too long intervals will not be penalized.
        boolean longTimeAgo = preRoundData.getRoundIndex() < (roundData.getRoundIndex() - 1);
        if (longTimeAgo && yellowPunishTx == null) {
            return ValidateResult.getSuccessResult();
        }
        if (isContinuous) {
            if (yellowPunishTx == null) {
                return ValidateResult.getSuccessResult();
            } else {
                return ValidateResult.getFailedResult("the block shouldn't has any yellow punish tx!");
            }
        }
        try {
            YellowPunishTransaction localTx = ConsensusTool.createYellowPunishTx(preBlock, member, round);
            if (null == localTx || !localTx.getHash().equals(yellowPunishTx.getHash())) {
                return ValidateResult.getFailedResult("the block has wrong yellow punish tx!");
            }
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(e.getMessage());
        }
        return ValidateResult.getSuccessResult();
    }


    private ValidateResult checkCoinBaseTx(List<Transaction> txs, BlockRoundData roundData, PocMeetingRound localRound) {
        Transaction tx = txs.get(0);
        if (tx.getType() != TransactionConstant.TX_TYPE_COIN_BASE) {
            return ValidateResult.getFailedResult("Coinbase transaction order wrong!");
        }

        for (int i = 1; i < txs.size(); i++) {
            Transaction transaction = txs.get(i);
            if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                ValidateResult result = ValidateResult.getFailedResult("Coinbase transaction more than one!");
                result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
                return result;
            }
        }
        if (null == localRound) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "local round data lost!");
        }
        PocMeetingMember member = localRound.getMember(roundData.getPackingIndexOfRound());
        CoinBaseTransaction coinBaseTx = (CoinBaseTransaction) tx;
        CoinBaseTransaction checkCoinBaseTx = ConsensusTool.createCoinBaseTx(member, txs.subList(1, txs.size()), localRound);
        if (checkCoinBaseTx.getHash().equals(coinBaseTx.getHash())) {
            return ValidateResult.getSuccessResult();
        }

        return ValidateResult.getFailedResult("Consensus reward calculation error.");
    }

    public BlockService getBlockService() {
        if (null == blockService) {
            blockService = NulsContext.getServiceBean(BlockService.class);
        }
        return blockService;
    }
}
