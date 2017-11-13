package io.nuls.core.chain.entity.transaction;

import io.nuls.core.chain.entity.NulsData;
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

    private CoinTransaction parent;
    //上次的输出
    private List<TransactionOutput> froms;

    private byte[] scriptBytes;

    private Script scriptSig;


    public TransactionInput() {
        this.froms = new ArrayList<>();
    }

    public TransactionInput(CoinTransaction parent) {
        this();
        this.parent = parent;
    }

    public TransactionInput(CoinTransaction parent, TransactionOutput output) {
        this();
        this.parent = parent;
        this.froms.add(output);
    }

    public TransactionInput(CoinTransaction parent, List<TransactionOutput> froms) {
        this.parent = parent;
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
                stream.write(from.getParent().getHash().getReversedBytes());
                Utils.uint32ToByteStreamLE(from.getIndex(), stream);
            }
        }
        //签名的长度
        stream.write(new VarInt(scriptBytes.length).encode());
        //签名
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
            CoinTransaction t = new CoinTransaction();
            t.setHash(byteBuffer.readHash());
            pre.setParent(t);
            pre.setIndex((int) byteBuffer.readUint32());
            froms.add(pre);
        }
        //输入签名的长度
        int signLength = (int) byteBuffer.readVarInt();
        scriptBytes = byteBuffer.readBytes(signLength);
        scriptSig = new Script(scriptBytes);
    }

    @Override
    public void verify() throws NulsException {

    }

    public CoinTransaction getParent() {
        return parent;
    }

    public void setParent(CoinTransaction parent) {
        this.parent = parent;
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
