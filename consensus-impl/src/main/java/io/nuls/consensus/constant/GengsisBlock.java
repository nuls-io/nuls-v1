package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.io.ByteBuffer;

/**
 * Created by Niels on 2017/11/10.
 *
 */
public final class GengsisBlock extends Block {
    private static final GengsisBlock instance = new GengsisBlock();

    private GengsisBlock() {
        //todo create Gengsis Block
        this.parse(new ByteBuffer(new byte[2]));
    }

    public static GengsisBlock getInstance() {
        return instance;
    }

}
