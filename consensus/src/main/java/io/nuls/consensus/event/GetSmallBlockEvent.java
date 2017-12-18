package io.nuls.consensus.event;

import com.sun.deploy.util.ArrayUtil;
import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.AskSmallBlockData;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetSmallBlockEvent extends BaseConsensusEvent<AskSmallBlockData> {


    public GetSmallBlockEvent() {
        super(ConsensusEventType.GET_SMALL_BLOCK);
    }


    @Override
    protected AskSmallBlockData parseEventBody(NulsByteBuffer byteBuffer) {
        AskSmallBlockData data = new AskSmallBlockData();
        data.parse(byteBuffer);
        return data;
    }

}