package io.nuls.network.message.entity;

import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionMessage extends AbstractNetworkMessage {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 0001;

    private long nonce;

    public GetVersionMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_GET_VERSION_MESSAGE;
        nonce = Utils.randomLong();
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(type);
        s += VarInt.sizeOf(nonce);
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(type).encode());
        stream.write(new VarInt(nonce).encode());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        type = (short) byteBuffer.readVarInt();
        nonce = byteBuffer.readByte();
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
