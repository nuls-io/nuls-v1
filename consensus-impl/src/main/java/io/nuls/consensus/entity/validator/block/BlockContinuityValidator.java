package io.nuls.consensus.entity.validator.block;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockContinuityValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "block continuity check failed";
    public static final BlockContinuityValidator INSTANCE = new BlockContinuityValidator();
    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);

    private BlockContinuityValidator() {
    }

    public static BlockContinuityValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed;
        do {
            if (block.getHeader().getHeight() == 0) {
                failed = !block.getHeader().getPreHash().equals(NulsDigestData.EMPTY_HASH);
                break;
            }
            Block preBlock = blockService.getBlock(block.getHeader().getHeight());
            failed = preBlock.getHeader().getHash().equals(block.getHeader().getPreHash());
            if(failed){
                break;
            }
            failed = preBlock.getHeader().getTime()==(block.getHeader().getTime()- PocConsensusConstant.BLOCK_TIME_INTERVAL*1000);
        } while (false);

        if (failed) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }
}
