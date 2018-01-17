package io.nuls.consensus.entity.validator.block.header;

import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.meeting.PocMeetingMember;
import io.nuls.consensus.entity.meeting.PocMeetingRound;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class HeaderPackerValidator implements NulsDataValidator<BlockHeader> {
    private static final String ERROR_MESSAGE = "block header packer check failed";
    public static final HeaderPackerValidator INSTANCE = new HeaderPackerValidator();
    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private ConsensusManager consensusManager = ConsensusManager.getInstance();

    private HeaderPackerValidator() {
    }

    public static HeaderPackerValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(BlockHeader header) {
        BlockHeader preHeader = blockService.getBlockHeader(header.getPreHash());
        BlockRoundData roundData = null;
        try {
            roundData = new BlockRoundData(preHeader.getExtend());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getSuccessResult();
        }
        PocMeetingRound currentRound = consensusManager.getCurrentRound();
        if (currentRound.getIndex() == roundData.getRoundIndex() && currentRound.getMemberCount() > (roundData.getPackingIndexOfRound() + 1)) {
            if (currentRound.indexOf(header.getPackingAddress()) <= roundData.getPackingIndexOfRound()) {
                return ValidateResult.getFailedResult(ERROR_MESSAGE);
            }
        } else if (currentRound.getIndex() == (1 + roundData.getRoundIndex())) {
            if (currentRound.getPreviousRound().indexOf(header.getPackingAddress()) <= roundData.getPackingIndexOfRound()) {
                return ValidateResult.getFailedResult(ERROR_MESSAGE);
            }
        }
        BlockRoundData nowRoundData = null;
        try {
            nowRoundData = new BlockRoundData(header.getExtend());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        if (!isAdjacent(roundData, nowRoundData)) {
            return ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }

    private boolean isAdjacent(BlockRoundData roundData, BlockRoundData nowRoundData) {
        if (roundData.getRoundIndex() == nowRoundData.getRoundIndex()) {
            return roundData.getPackingIndexOfRound() + 1 == nowRoundData.getPackingIndexOfRound();
        } else if (roundData.getRoundIndex() + 1 == nowRoundData.getRoundIndex()) {
            return 1 == nowRoundData.getPackingIndexOfRound();
        }
        return false;
    }
}
