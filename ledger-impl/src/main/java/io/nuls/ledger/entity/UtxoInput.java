package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class UtxoInput extends BaseNulsData {

    private NulsDigestData txHash;
    /**
     * the output last time
     */
    private List<UtxoOutput> froms;
    private NulsSignData sign;

    public UtxoInput() {
        this.froms = new ArrayList<>();
    }

    public UtxoInput(NulsDigestData txHash) {
        this();
        this.txHash = txHash;
    }

    public UtxoInput(NulsDigestData txHash, UtxoOutput output) {
        this();
        this.txHash = txHash;
        this.froms.add(output);
    }

    public UtxoInput(NulsDigestData txHash, List<UtxoOutput> froms) {
        this.txHash = txHash;
        this.froms = froms;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (froms == null || froms.size() == 0) {
            stream.write(new VarInt(0).encode());
        } else {
            stream.write(new VarInt(froms.size()).encode());
            for (UtxoOutput from : froms) {
                stream.write(from.getTxHash().getDigestBytes());
                stream.writeShort((short) from.getIndex());
            }
        }
        //sign
        stream.write(sign.serialize());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer == null) {
            return;
        }
        froms = new ArrayList<>();

        int fromSize = (int) byteBuffer.readVarInt();
        for (int i = 0; i < fromSize; i++) {
            UtxoOutput pre = new UtxoOutput();
            pre.setTxHash(byteBuffer.readHash());
            pre.setIndex((int) byteBuffer.readUint32());
            froms.add(pre);
        }
        //length of sign
        sign = new NulsSignData();
        sign.parse(byteBuffer);
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }

    public List<UtxoOutput> getFroms() {
        return froms;
    }

    public void setFroms(List<UtxoOutput> froms) {
        this.froms = froms;
    }

}
