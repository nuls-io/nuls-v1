package io.nuls.consensus.service.intf;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public interface BlockService {

    Block getGengsisBlock();

    long getLocalHeight();

    Block getLocalBestBlock();

    BlockHeader getBlockHeader(long height);

    Block getBlock(String hash);

    Block getBlock(long height);

    List<Block> getBlockList(long startHeight, long endHeight);

    void saveBlock(Block block) throws IOException;

    void rollbackBlock(long height) throws NulsException;

    int getBlockCount(String address, long roundStart, long index);

    List<NulsDigestData> getBlockHashList(long start, long end, long split);
}
