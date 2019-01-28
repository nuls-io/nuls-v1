package io.nuls.consensus.poc.storage.service;

import io.nuls.consensus.poc.storage.po.RandomSeedPo;
import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;

import java.util.List;

/**
 * @author Niels
 */
public interface RandomSeedsStorageService {

    RandomSeedStatusPo getAddressStatus(byte[] address);

    boolean saveAddressStatus(byte[] address, long nowHeight, byte[] nextSeed, byte[] seedHash);

    boolean saveRandomSeed(long height, long preHeight, byte[] seed, byte[] nextSeedHash);

    boolean deleteRandomSeed(long height);

    List<byte[]> getSeeds(long maxHeight, int seedCount);

    List<byte[]> getSeeds(long startHeight, long endHeight);

    void deleteAddressStatus(byte[] address);

    RandomSeedPo getSeed(long height);
}
