package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.constant.ConsensusStorageConstant;
import io.nuls.consensus.poc.storage.po.RandomSeedPo;
import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
@Component
public class RandomSeedsStorageServiceImpl implements RandomSeedsStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public RandomSeedStatusPo getAddressStatus(byte[] address) {
        byte[] bytes = dbService.get(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, address);
        if (null == bytes) {
            return null;
        }
        RandomSeedStatusPo po = new RandomSeedStatusPo();
        try {
            po.parse(new NulsByteBuffer(bytes, 0));
            po.setAddress(address);
        } catch (NulsException e) {
            Log.error(e);
        }
        return po;
    }

    @Override
    public boolean saveAddressStatus(byte[] address, long nowHeight, byte[] nextSeed, byte[] seedHash) {
        RandomSeedStatusPo po = new RandomSeedStatusPo();
        po.setHeight(nowHeight);
        po.setNextSeed(nextSeed);
        po.setSeedHash(seedHash);
        try {
            dbService.put(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, address, po.serialize());
            return true;
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public void deleteAddressStatus(byte[] address) {
        dbService.delete(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, address);
    }

    @Override
    public boolean saveRandomSeed(long height, long preHeight, byte[] seed, byte[] nextSeedHash) {
        RandomSeedPo po = new RandomSeedPo();
        po.setPreHeight(preHeight);
        po.setSeed(seed);
        po.setNextSeedHash(nextSeedHash);
        try {
            dbService.put(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, SerializeUtils.uint64ToByteArray(height), po.serialize());
            return true;
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean deleteRandomSeed(long height) {
        dbService.delete(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, SerializeUtils.uint64ToByteArray(height));
        return true;
    }

    @Override
    public RandomSeedPo getSeed(long height) {
        byte[] bytes = dbService.get(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS, SerializeUtils.uint64ToByteArray(height));
        if (null == bytes) {
            return null;
        }
        RandomSeedPo po = new RandomSeedPo();
        try {
            po.parse(new NulsByteBuffer(bytes, 0));
            po.setHeight(height);
        } catch (NulsException e) {
            Log.error(e);
        }
        return po;
    }

    @Override
    public List<byte[]> getSeeds(long maxHeight, int seedCount) {
        List<byte[]> list = new ArrayList<>();
        while (maxHeight > 0) {
            RandomSeedPo po = getSeed(maxHeight--);
            if (null != po && !ArraysTool.arrayEquals(po.getSeed(), ConsensusStorageConstant.EMPTY_SEED)) {
                list.add(po.getSeed());
            }
            if (list.size() >= seedCount) {
                break;
            }
        }
        return list;
    }

    @Override
    public List<byte[]> getSeeds(long startHeight, long endHeight) {
        List<byte[]> list = new ArrayList<>();
        long height = startHeight;
        while (height <= endHeight) {
            RandomSeedPo po = getSeed(height++);
            if (null != po && !ArraysTool.arrayEquals(po.getSeed(), ConsensusStorageConstant.EMPTY_SEED)) {
                list.add(po.getSeed());
            }
        }
        return list;
    }

    @Override
    public void afterPropertiesSet() {
        dbService.createArea(ConsensusStorageConstant.DB_NAME_RANDOM_SEEDS);
    }
}
