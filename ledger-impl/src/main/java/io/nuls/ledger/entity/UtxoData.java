package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Niels on 2017/11/16.
 */
public class UtxoData extends BaseNulsData{
    private List<UtxoInput> inputs;
    private List<UtxoOutput> outputs;

    public List<UtxoInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<UtxoInput> inputs) {
        this.inputs = inputs;
    }

    public List<UtxoOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<UtxoOutput> outputs) {
        this.outputs = outputs;
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        //todo

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo

    }
}
