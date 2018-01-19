/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.validator.UtxoTxInputsValidator;
import io.nuls.ledger.validator.UtxoTxOutputsValidator;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class UtxoData extends CoinData{
    public UtxoData(){
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
        //todo
        return 0;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) {
        //todo
    }
}
