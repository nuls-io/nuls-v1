package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author win10
 * @date 2017/10/30
 */
public class UtxoInput extends BaseNulsData {

    private NulsDigestData txHash;
    /**
     * the output last time
     */

    private UtxoOutput from;

    private NulsSignData sign;

    private Transaction parent;

    public UtxoInput() {

    }

    public UtxoInput(NulsDigestData txHash) {
        this();
        this.txHash = txHash;
    }

    public UtxoInput(NulsDigestData txHash, UtxoOutput output) {
        this();
        this.txHash = txHash;
        this.from = output;
    }

    public UtxoInput(NulsDigestData txHash, UtxoOutput from, Transaction parent) {
        this(txHash, from);
        this.parent = parent;
    }

    @Override
    public int size() {

        return Utils.sizeOfSerialize(sign);
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

        //sign
        stream.writeNulsData(sign);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer == null) {
            return;
        }

        //length of sign
        sign = byteBuffer.readSign();
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

    public UtxoOutput getFrom() {
        return from;
    }

    public void setFrom(UtxoOutput from) {
        this.from = from;
    }

    public Transaction getParent() {
        return parent;
    }

    public void setParent(Transaction parent) {
        this.parent = parent;
    }

}
