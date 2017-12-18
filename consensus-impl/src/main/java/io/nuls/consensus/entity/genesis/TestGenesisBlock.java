package io.nuls.consensus.entity.genesis;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.utils.date.DateUtil;

/**
 * @author Niels
 * @date 2017/11/10
 */
public final class TestGenesisBlock extends Block {

    private static final TestGenesisBlock INSTANCE = new TestGenesisBlock();

    public static TestGenesisBlock getInstance() {
        return INSTANCE;
    }

    private TestGenesisBlock() {
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
