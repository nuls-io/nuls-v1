package io.nuls.ledger.entity.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;

/**
 * @author Niels
 * @date 2017/11/10
 */
public final class DevGenesisBlock extends Block {

    private static final DevGenesisBlock INSTANCE = new DevGenesisBlock();

    public static DevGenesisBlock getInstance() {
        return INSTANCE;
    }

    private DevGenesisBlock() {
        this.setHeader(new BlockHeader());
        initGengsisTxs();
        fillHeader();
    }

    private void fillHeader() {
        //todo temp
        this.getHeader().setHash(new NulsDigestData());
    }

    private void initGengsisTxs() {
        //todo
    }

}
