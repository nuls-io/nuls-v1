package io.nuls.ledger.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class UtxoData extends CoinData{
    public UtxoData(){
        //todo
//        this.registerValidator();
    }
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
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo

    }
}
