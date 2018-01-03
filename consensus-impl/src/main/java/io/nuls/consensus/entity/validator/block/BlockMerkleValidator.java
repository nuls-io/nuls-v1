package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockMerkleValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "Merkle Hash is wrong!";
    public static final BlockMerkleValidator INSTANCE = new BlockMerkleValidator();
    private BlockMerkleValidator(){}
    public static BlockMerkleValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(Block data) {
        ValidateResult result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        do {
            if (null == data) {
                result.setMessage("Data is null!");
                break;
            }
            if (data.getHeader().getMerkleHash().equals(buildMerkleHash(data))) {
                break;
            }
            result = ValidateResult.getSuccessResult();
        } while (false);
        return result;
    }

    private NulsDigestData buildMerkleHash(Block data) {
        //todo
        List<NulsDigestData> txHashList = data.getTxHashList();
        //todo temp
        return data.getHeader().getMerkleHash();
    }
}
