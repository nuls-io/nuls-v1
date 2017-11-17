package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/16.
 */
public class UtxoData extends NulsData{
    private UtxoInput inputs;
    private UtxoOutput outputs;

    public UtxoInput getInputs() {
        return inputs;
    }

    public void setInputs(UtxoInput inputs) {
        this.inputs = inputs;
    }

    public UtxoOutput getOutputs() {
        return outputs;
    }

    public void setOutputs(UtxoOutput outputs) {
        this.outputs = outputs;
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
