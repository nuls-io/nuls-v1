package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/10
 *
 */
public final class GengsisBlock extends Block {
    private static final GengsisBlock INSTANCE = new GengsisBlock();

    private GengsisBlock() {
//        super(0, TimeService.currentTimeMillis());
        //todo create Gengsis Block
        this.parse(new NulsByteBuffer(new byte[2]));
    }

    public static GengsisBlock getInstance() {
        return INSTANCE;
    }

}
