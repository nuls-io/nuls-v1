package io.nuls.consensus.entity.validator.block;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class BlockPackerValidator implements NulsDataValidator<Block> {
    private static BlockPackerValidator INSTANCE = new BlockPackerValidator();private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private BlockPackerValidator(){}
    public static BlockPackerValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(Block block) {
        BlockHeader preHeader = blockService.getBlockHeader(block.getHeader().getPreHash());

        return null;
    }
}
