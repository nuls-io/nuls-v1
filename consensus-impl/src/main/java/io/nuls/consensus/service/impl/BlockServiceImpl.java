package io.nuls.consensus.service.impl;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockServiceImpl implements BlockService {
    @Override
    public Block getGengsisBlock() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public int getLocalHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public byte[] getLocalHighestHash() {
        // todo auto-generated method stub(niels)
        return new byte[0];
    }

    @Override
    public int getBestHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public byte[] getBestHash() {
        // todo auto-generated method stub(niels)
        return new byte[0];
    }

    @Override
    public Block getLocalHighestBlock() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBestBlock() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBlockByHash(String hash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBlockByHeight(int height) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
