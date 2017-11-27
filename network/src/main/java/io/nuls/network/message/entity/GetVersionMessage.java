package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionMessage extends AbstractNetworkMessage {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private long nonce;

    public GetVersionMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_GET_VERSION_MESSAGE;
        nonce = Utils.randomLong();
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(this.version.getVersion());
        s += VarInt.sizeOf(type);
        s += VarInt.sizeOf(nonce);
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version.getVersion()).encode());
        stream.write(new VarInt(type).encode());
        stream.write(new VarInt(nonce).encode());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        short version = (short) byteBuffer.readVarInt();
        this.setVersion(new NulsVersion(version));
        type = (short) byteBuffer.readVarInt();
        nonce = byteBuffer.readVarInt();
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }


    public static void main(String[] args) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(NulsMessage.MAX_SIZE);


        NulsMessageHeader header = new NulsMessageHeader(12345678, (short) 1, 500, (byte) 3);
        VersionMessage versionMessage = new VersionMessage(50000, "abcdefghij");
        System.out.println(versionMessage.toString());
        NulsMessage nulsMessage = new NulsMessage(header, versionMessage.serialize());

        byte[] messages = nulsMessage.serialize();
        readBuffer.put(messages);
        System.out.println(header.toString());
        System.out.println("---------");

        readBuffer.flip();

        byte[] headers = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE];
        readBuffer.get(headers, 0, headers.length);

        header = new NulsMessageHeader();
        header.parse(new NulsByteBuffer(headers));
        System.out.println(header.toString());


        byte[] data = new byte[readBuffer.limit() - headers.length];
        readBuffer.get(data, 0, data.length);
        versionMessage = new VersionMessage();

        versionMessage.parse(new NulsByteBuffer(data));
        System.out.println(versionMessage.toString());

    }
}
