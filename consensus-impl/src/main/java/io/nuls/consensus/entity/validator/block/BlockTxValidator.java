package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class














BlockTxValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "";
    public static final BlockTxValidator INSTANCE = new BlockTxValidator();

    private BlockTxValidator() {
    }

    public static BlockTxValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        if (block.getHeader().getTxCount() != block.getTxs().size()) {
            return ValidateResult.getFailedResult("txCount is wrong!");
        }
        int count = 0;
        for (Transaction tx : block.getTxs()) {
            ValidateResult result = tx.verify();
            if (null==result||result.isFailed()) {
                return ValidateResult.getFailedResult("there is wrong transaction!");
            }
            if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                count++;
            }
        }
        if (count > 1) {
            return ValidateResult.getFailedResult("coinbase transaction must only one!");
        }
        return ValidateResult.getSuccessResult();
    }
}
