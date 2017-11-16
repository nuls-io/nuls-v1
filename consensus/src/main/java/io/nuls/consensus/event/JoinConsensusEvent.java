package io.nuls.consensus.event;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/7.
 *
 */
//todo
public class JoinConsensusEvent extends BaseConsensusEvent {

    public JoinConsensusEvent(NulsEventHeader header) {
        super(header);
    }

    @Override
    protected NulsData parseEventBody(ByteBuffer byteBuffer) {
        //todo
        return null;
    }


}
