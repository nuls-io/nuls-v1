package io.nuls.consensus.service.intf;

import io.nuls.core.chain.entity.Block;

/**
 * Created by Niels on 2017/11/10.
 * nuls.io
 */
public interface BlockService {

    Block getGengsisBlock();

    int getLocalHeight();

    byte[] getLocalHighestHash();

    int getBestHeight();

    byte[] getBestHash();

    Block getLocalHighestBlock();

    Block getBestBlock();
    
    
}
