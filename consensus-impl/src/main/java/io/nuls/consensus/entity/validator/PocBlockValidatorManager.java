package io.nuls.consensus.entity.validator;

import io.nuls.consensus.entity.validator.block.*;
import io.nuls.consensus.entity.validator.block.header.HeaderFieldValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderHashValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderPackerValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderSignValidator;
import io.nuls.core.chain.manager.BlockHeaderValidatorManager;
import io.nuls.core.chain.manager.BlockValidatorManager;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class PocBlockValidatorManager {

    public static void initHeaderValidators(){
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderFieldValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderHashValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderPackerValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderSignValidator.getInstance());
    }

    public static void initBlockValidators() {
        //todo
        BlockValidatorManager.addBlockDefValitor(BlockHeaderValidator.getInstance());
        BlockValidatorManager.addBlockDefValitor(BlockFieldValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockContinuityValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockPackerValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockMaxSizeValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockTxValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockMerkleValidator.getInstance());

    }
}
