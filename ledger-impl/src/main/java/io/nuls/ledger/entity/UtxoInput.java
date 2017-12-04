package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win10 on 2017/10/30.
 */
public class UtxoInput extends BaseNulsData {

    private Sha256Hash txHash;
    //the output last time
    private List<UtxoOutput> froms;

    private byte[] scriptBytes;

    private Script scriptSig;


    public UtxoInput() {
        this.froms = new ArrayList<>();
    }

    public UtxoInput(Sha256Hash txHash) {
        this();
        this.txHash = txHash;
    }

    public UtxoInput(Sha256Hash txHash, UtxoOutput output) {
        this();
        this.txHash = txHash;
        this.froms.add(output);
    }

    public UtxoInput(Sha256Hash txHash, List<UtxoOutput> froms) {
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
                stream.write(from.getTxHash().getReversedBytes());
                stream.writeShort((short) from.getIndex());
            }
        }
        //length of sign
        stream.write(new VarInt(scriptBytes.length).encode());
        //sign
        stream.write(scriptBytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
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
        int signLength = (int) byteBuffer.readVarInt();
        scriptBytes = byteBuffer.readBytes(signLength);
        scriptSig = new Script(scriptBytes);
    }

    public Sha256Hash getTxHash() {
        return txHash;
    }

    public void setTxHash(Sha256Hash txHash) {
        this.txHash = txHash;
    }

    public List<UtxoOutput> getFroms() {
        return froms;
    }

    public void setFroms(List<UtxoOutput> froms) {
        this.froms = froms;
    }

    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    public void setScriptBytes(byte[] scriptBytes) {
        this.scriptBytes = scriptBytes;
    }

    public Script getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(Script scriptSig) {
        this.scriptSig = scriptSig;
    }
}
