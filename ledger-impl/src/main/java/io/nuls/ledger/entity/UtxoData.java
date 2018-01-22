/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.validator.UtxoTxInputsValidator;
import io.nuls.ledger.validator.UtxoTxOutputsValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class UtxoData extends CoinData {
    public UtxoData() {
        this.registerValidator(UtxoTxInputsValidator.getInstance());
        this.registerValidator(UtxoTxOutputsValidator.getInstance());
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
        int size = 0;

        size += 1;   //input's length
        if (inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                size += inputs.get(i).size();
            }
        }

        size += 1;   //output's length
        if (outputs != null) {
            for (int i = 0; i < outputs.size(); i++) {
                size += outputs.get(i).size();
            }
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(inputs == null ? 0 : inputs.size());
        if (inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).serializeToStream(stream);
            }
        }
        stream.writeVarInt(outputs == null ? 0 : outputs.size());
        if (outputs != null) {
            for (int i = 0; i < outputs.size(); i++) {
                outputs.get(i).serializeToStream(stream);
            }
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int size = (int) byteBuffer.readVarInt();
        List<UtxoInput> inputs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UtxoInput input = new UtxoInput();
            input.parse(byteBuffer);
            inputs.add(input);
        }

        size = (int) byteBuffer.readVarInt();
        List<UtxoOutput> outputs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UtxoOutput output = new UtxoOutput();
            output.parse(byteBuffer);
            outputs.add(output);
        }
    }
}
