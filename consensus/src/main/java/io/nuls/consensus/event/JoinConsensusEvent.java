package io.nuls.consensus.event;

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
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {

    }

}
