package io.nuls.consensus.poc.service.impl;

import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.storage.constant.ConsensusStorageConstant;
import io.nuls.consensus.poc.storage.po.RandomSeedPo;
import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.consensus.poc.util.RandomSeedUtils;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;

/**
 * @author Niels
 */
@Component
public class RandomSeedService {

    @Autowired
    private RandomSeedsStorageService randomSeedsStorageService;

    public void processBlock(BlockHeader header, BlockHeader preHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(header.getExtend());
        byte[] nextSeed = null;
        if (ArraysTool.arrayEquals(header.getPackingAddress(), RandomSeedUtils.CACHE_SEED.getAddress())) {
            nextSeed = RandomSeedUtils.CACHE_SEED.getNextSeed();
        }

        byte[] seed = extendsData.getSeed();
        RandomSeedStatusPo po = this.randomSeedsStorageService.getAddressStatus(header.getPackingAddress());
        long preHeight = 0;
        if (null == po || ArraysTool.arrayEquals(preHeader.getPackingAddress(), header.getPackingAddress()) || !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
            seed = ConsensusStorageConstant.EMPTY_SEED;
        }
        if (null != po) {
            preHeight = po.getHeight();
        }
        randomSeedsStorageService.saveAddressStatus(header.getPackingAddress(), header.getHeight(), nextSeed, extendsData.getNextSeedHash());
        randomSeedsStorageService.saveRandomSeed(header.getHeight(), preHeight, seed, extendsData.getNextSeedHash());
    }

    public void rollbackBlock(BlockHeader header) {
        RandomSeedPo po = randomSeedsStorageService.getSeed(header.getHeight());
        randomSeedsStorageService.deleteRandomSeed(header.getHeight());
        if (null == po || po.getPreHeight() == 0L) {
            randomSeedsStorageService.deleteAddressStatus(header.getPackingAddress());
        } else {
            randomSeedsStorageService.saveAddressStatus(header.getPackingAddress(), po.getHeight(), po.getSeed(), po.getNextSeedHash());
        }
    }
}
