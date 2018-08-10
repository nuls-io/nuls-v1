package io.nuls.protocol.model.tx;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.Set;

/**
 * @author: Niels Wang
 * @date: 2018/7/24
 */
public class LogicData extends TransactionLogicData {

    private byte[] bytes;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(bytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        bytes = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        return SerializeUtils.sizeOfBytes(bytes);
    }

    @Override
    public Set<byte[]> getAddresses() {
        return null;
    }

}
