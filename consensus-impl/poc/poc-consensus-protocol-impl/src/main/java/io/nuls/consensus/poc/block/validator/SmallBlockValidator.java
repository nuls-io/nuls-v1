package io.nuls.consensus.poc.block.validator;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.SmallBlock;

/**
 * @author: Niels Wang
 * @date: 2018/4/19
 */
public class SmallBlockValidator implements NulsDataValidator<SmallBlock> {
    /**
     * @param data
     * @return
     */
    @Override
    public ValidateResult validate(SmallBlock data) {
        if (null == data || data.getHeader() == null || null == data.getTxHashList() || null == data.getSubTxList() || data.getTxHashList().isEmpty() || data.getSubTxList().isEmpty()) {
            return ValidateResult.getFailedResult(ErrorCode.DATA_ERROR, "the small block has null field");
        }

        ValidateResult result = data.getHeader().verify();
        if (result.isFailed()) {
            return result;
        }
        if (!data.getHeader().getMerkleHash().equals(NulsDigestData.calcMerkleDigestData(data.getTxHashList()))) {
            return ValidateResult.getFailedResult("the MerkleHash is wrong:" + data.getHeader().getHash());
        }
        return ValidateResult.getSuccessResult();
    }
}
