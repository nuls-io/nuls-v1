package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.manager.BlockValidatorManager;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class PocBlockValidatorManager {

    public static void initBlockValidators() {
        //todo
        BlockValidatorManager.addBlockDefValitor(BlockFieldValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockContinuityValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockPackerValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockSignValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockMaxSizeValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockTxValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockMerkleValidator.getInstance());

    }
}
