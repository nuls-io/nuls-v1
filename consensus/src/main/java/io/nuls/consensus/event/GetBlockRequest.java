package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlockRequest extends BaseConsensusEvent<BasicTypeData<byte[]>> {
    private long start;
    private long end;

    public GetBlockRequest() {
        super(ConsensusEventType.GET_BLOCK);
    }

    public GetBlockRequest(long start, long end) {
        this();
        byte[] bytes1 = new VarInt(start).encode();
        byte[] bytes2 = new VarInt(end).encode();
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        BasicTypeData<byte[]> data = new BasicTypeData<>(bytes);
        this.setEventBody(data);
    }

    @Override
    protected BasicTypeData<byte[]> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        BasicTypeData<byte[]> data = byteBuffer.readNulsData(new BasicTypeData<>());
        NulsByteBuffer buffer = new NulsByteBuffer(data.getVal());
        this.start = buffer.readVarInt();
        this.end = buffer.readVarInt();
        return data;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}