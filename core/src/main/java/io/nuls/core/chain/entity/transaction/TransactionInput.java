package io.nuls.core.chain.entity.transaction;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win10 on 2017/10/30.
 */
public class TransactionInput extends NulsData {

    private Sha256Hash txHash;
    //the output last time
    private List<TransactionOutput> froms;

    private byte[] scriptBytes;

    private Script scriptSig;


    public TransactionInput() {
        this.froms = new ArrayList<>();
    }

    public TransactionInput(Sha256Hash txHash) {
        this();
        this.txHash = txHash;
    }

    public TransactionInput(Sha256Hash txHash, TransactionOutput output) {
        this();
        this.txHash = txHash;
        this.froms.add(output);
    }

    public TransactionInput(Sha256Hash txHash, List<TransactionOutput> froms) {
        this.txHash = txHash;
        this.froms = froms;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        if (froms == null || froms.size() == 0) {
            stream.write(new VarInt(0).encode());
        } else {
            stream.write(new VarInt(froms.size()).encode());
            for (TransactionOutput from : froms) {
                stream.write(from.getTxHash().getReversedBytes());
                Utils.uint32ToByteStreamLE(from.getIndex(), stream);
            }
        }
        //length of sign
        stream.write(new VarInt(scriptBytes.length).encode());
        //sign
        stream.write(scriptBytes);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        froms = new ArrayList<>();

        int fromSize = (int) byteBuffer.readVarInt();
        for (int i = 0; i < fromSize; i++) {
            TransactionOutput pre = new TransactionOutput();
            pre.setTxHash(byteBuffer.readHash());
            pre.setIndex((int) byteBuffer.readUint32());
            froms.add(pre);
        }
        //length of sign
        int signLength = (int) byteBuffer.readVarInt();
        scriptBytes = byteBuffer.readBytes(signLength);
        scriptSig = new Script(scriptBytes);
    }

    @Override
    public void verify() throws NulsException {

    }

    public Sha256Hash getTxHash() {
        return txHash;
    }

    public void setTxHash(Sha256Hash txHash) {
        this.txHash = txHash;
    }

    public List<TransactionOutput> getFroms() {
        return froms;
    }

    public void setFroms(List<TransactionOutput> froms) {
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
