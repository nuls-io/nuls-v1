package io.nuls.consensus.entity.member;

import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusAccountImpl extends ConsensusAccount<ConsensusAccountData> {

    @Override
    protected ConsensusAccountData parseExtend(NulsByteBuffer byteBuffer) {
        ConsensusAccountData data = new ConsensusAccountData();
        data.parse(byteBuffer);
        return data;
    }
}


