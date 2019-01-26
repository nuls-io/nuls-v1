package io.nuls.consensus.poc.block.validator;

import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.storage.constant.ConsensusStorageConstant;
import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.consensus.poc.util.RandomSeedUtils;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.constant.ProtocolErroeCode;

/**
 * @author Niels
 */
@Component
public class RandomSeedValidator implements NulsDataValidator<BlockHeader> {

    @Autowired
    private RandomSeedsStorageService randomSeedsStorageService;

    @Override
    public ValidateResult validate(BlockHeader data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        if (NulsVersionManager.getCurrentVersion() < 3) {
            return result;
        }
        BlockExtendsData extendsData = new BlockExtendsData(data.getExtend());
        boolean failed = false;
        do {
            if (extendsData.getNextSeedHash() == null || extendsData.getSeed() == null) {
                failed = true;
                break;
            }
            RandomSeedStatusPo po = this.randomSeedsStorageService.getAddressStatus(data.getPackingAddress());
            if (null == po && !ArraysTool.arrayEquals(ConsensusStorageConstant.EMPTY_SEED, extendsData.getSeed())) {
                failed = true;
                break;
            }
            if (null != po && !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
                failed = true;
                break;
            }
        } while (false);
        if (failed) {
            Log.warn("recieve a wrong random number seed,{}", data.getHash());
            result = ValidateResult.getFailedResult(this.getClass().getName(), ProtocolErroeCode.BLOCK_HEADER_FIELD_CHECK_FAILED);
        }
        return result;
    }

}
