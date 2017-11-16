package io.nuls.consensus.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class RedPunishConsensusEvent extends BaseConsensusEvent{
    public RedPunishConsensusEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected NulsData parseEventBody(ByteBuffer byteBuffer) {
        return null;
    }


}