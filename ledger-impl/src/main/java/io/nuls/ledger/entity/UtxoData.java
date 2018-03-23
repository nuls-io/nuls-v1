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

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class UtxoData extends CoinData {

    public UtxoData() {

    }

    private List<UtxoInput> inputs = new ArrayList<>();

    private List<UtxoOutput> outputs = new ArrayList<>();

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
        size += getListByteSize(inputs);
        size += getListByteSize(outputs);
        return size;
    }

    private int getListByteSize(List list) {
        int size = 0;
        if (list == null) {
            size += Utils.sizeOfInt(0);
        } else {
            size += Utils.sizeOfInt(list.size());
            for (int i = 0; i < list.size(); i++) {
                size += Utils.sizeOfNulsData((BaseNulsData) list.get(i));
            }
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(inputs == null ? 0 : inputs.size());
        if (inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                stream.writeNulsData(inputs.get(i));
            }
        }
        stream.writeVarInt(outputs == null ? 0 : outputs.size());
        if (outputs != null) {
            for (int i = 0; i < outputs.size(); i++) {
                stream.writeNulsData(outputs.get(i));
            }
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int size = (int) byteBuffer.readVarInt();
        inputs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UtxoInput input = byteBuffer.readNulsData(new UtxoInput());
            inputs.add(input);
        }

        size = (int) byteBuffer.readVarInt();
        outputs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UtxoOutput output = byteBuffer.readNulsData(new UtxoOutput());
            outputs.add(output);
        }

    }

    @Override
    public Na getTotalNa() {
        if(null==super.getTotalNa()) {
            Set<String> addressSet = new HashSet<>();

            if (null != this.getInputs()) {
                for (UtxoInput input : this.getInputs()) {
                    addressSet.add(input.getFrom().getAddress());
                }
            }
            Na totalNa = Na.ZERO;
            if (null != this.getOutputs()) {
                for (int i = 0; i < this.getOutputs().size(); i++) {
                    UtxoOutput output = this.getOutputs().get(i);
                    if (addressSet.contains(output.getAddress())) {
                        if ( i == 0&&output.isLocked()) {
                            totalNa.add(Na.valueOf(output.getValue()));
                        }
                    } else {
                        totalNa.add(Na.valueOf(output.getValue()));
                    }
                }
            }
            this.setTotalNa(totalNa);
        }
        return super.getTotalNa();
    }
}
