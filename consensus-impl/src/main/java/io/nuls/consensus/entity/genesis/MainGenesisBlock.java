package io.nuls.consensus.entity.genesis;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.utils.date.DateUtil;

/**
 * @author Niels
 * @date 2017/11/10
 */
public final class MainGenesisBlock extends Block {

    private static final MainGenesisBlock INSTANCE = new MainGenesisBlock();

    public static MainGenesisBlock getInstance() {
        return INSTANCE;
    }

    private MainGenesisBlock() {
        this.setHeader(new BlockHeader());
        initGengsisTxs();
        fillHeader();
    }

    private void fillHeader() {
        //todo temp
        this.getHeader().setHash(new NulsDigestData());
    }

    private void initGengsisTxs() {
        //todo 总nuls量及每个地址的nuls量
    }

}
