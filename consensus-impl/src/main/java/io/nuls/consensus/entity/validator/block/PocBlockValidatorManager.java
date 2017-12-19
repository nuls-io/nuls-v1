package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.manager.BlockValidatorManager;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class PocBlockValidatorManager {

    public static void initBlockValidators() {
        BlockValidatorManager.addBlockDefValitor(BlockSignValidator.getInstance());
        BlockValidatorManager.addBlockDefValitor(BlockMaxSizeValidator.getInstance());
        BlockValidatorManager.addBlockDefValitor(BlockTxValidator.getInstance());
        BlockValidatorManager.addBlockDefValitor(BlockMerkleValidator.getInstance());

    }
}
