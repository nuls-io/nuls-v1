package io.nuls.consensus.service.intf;

import io.nuls.core.chain.entity.Block;

/**
 *
 * @author Niels
 * @date 2017/11/10
 *
 */
public interface BlockService {

    Block getGengsisBlockFromDb();

    int getLocalHeight();

    byte[] getLocalHighestHash();

    int getBestHeight();

    byte[] getBestHash();

    Block getLocalHighestBlock();

    Block getBestBlock();

    Block getBlockByHash(String hash);

    Block getBlockByHeight(int height);

    void save(Block block);

    void clearLocalBlocks();
}
