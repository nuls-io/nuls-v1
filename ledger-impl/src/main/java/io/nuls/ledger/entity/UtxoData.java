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

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.service.impl.UtxoCoinManager;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

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
            size += Utils.sizeOfVarInt(0);
        } else {
            size += Utils.sizeOfVarInt(list.size());
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
    public Na getTotalNa() throws NulsException {
        if (null == this.totalNa) {
            Set<String> addressSet = new HashSet<>();
            if (null != this.getInputs()) {
                boolean b = false;
                for (UtxoInput input : this.getInputs()) {
                    b = false;
                    if (input.getFrom() == null) {
                        List<AbstractCoinTransaction> localTxs = UtxoCoinManager.getInstance().getLocalUnConfirmTxs();
                        for (AbstractCoinTransaction tx : localTxs) {
                            UtxoData utxoData = (UtxoData) tx.getCoinData();
                            for (UtxoOutput output : utxoData.getOutputs()) {
                                if (input.getKey().equals(output.getKey())) {
                                    input.setFrom(output);
                                    b = true;
                                    break;
                                }
                            }
                            if (b) {
                                break;
                            }
                        }
                        if (!b) {
                            throw new NulsException(ErrorCode.ORPHAN_TX);
                        }
                    }
                    addressSet.add(input.getFrom().getAddress());
                }
            }

            Na totalNa = Na.ZERO;
            if (null != this.getOutputs()) {
                for (int i = 0; i < this.getOutputs().size(); i++) {
                    UtxoOutput output = this.getOutputs().get(i);
                    if (addressSet.contains(output.getAddress())) {
                        if (i == 0 && (output.getStatus() == OutPutStatusEnum.UTXO_CONSENSUS_LOCK || output.getStatus() == OutPutStatusEnum.UTXO_TIME_LOCK)) {
                            totalNa = totalNa.add(Na.valueOf(output.getValue()));
                            break;
                        }
                    } else {
                        totalNa = totalNa.add(Na.valueOf(output.getValue()));
                    }
                }
            }
            this.setTotalNa(totalNa);
        }
        return this.totalNa;
    }
}
