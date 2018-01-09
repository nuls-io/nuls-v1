package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class BlockPackerValidator implements NulsDataValidator<Block> {
    private static BlockPackerValidator INSTANCE = new BlockPackerValidator();
    private BlockPackerValidator(){}
    public static BlockPackerValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(Block data) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
