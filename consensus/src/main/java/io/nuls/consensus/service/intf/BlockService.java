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

    long getLocalHeight();

    byte[] getLocalHighestHash();

    long getBestHeight();

    byte[] getBestHash();

    Block getLocalHighestBlock();

    Block getBestBlock();

    Block getBlockByHash(String hash);

    Block getBlockByHeight(long height);

    void save(Block block);

    void clearLocalBlocks();

    void rollback(long height);
}
