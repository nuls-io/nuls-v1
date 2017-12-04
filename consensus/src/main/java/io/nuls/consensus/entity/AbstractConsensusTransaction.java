package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.date.TimeService;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class AbstractConsensusTransaction <T extends BaseNulsData> extends Transaction {

    public AbstractConsensusTransaction(int type) {
        super(type);
    }

}