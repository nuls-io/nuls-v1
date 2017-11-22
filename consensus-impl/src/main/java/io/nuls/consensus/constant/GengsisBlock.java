package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * Created by Niels on 2017/11/10.
 *
 */
public final class GengsisBlock extends Block {
    private static final GengsisBlock INSTANCE = new GengsisBlock();

    private GengsisBlock() {
        //todo create Gengsis Block
        this.parse(new NulsByteBuffer(new byte[2]));
    }

    public static GengsisBlock getInstance() {
        return INSTANCE;
    }

}
