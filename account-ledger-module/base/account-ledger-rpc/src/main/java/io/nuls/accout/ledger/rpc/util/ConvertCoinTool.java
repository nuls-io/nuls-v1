/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.accout.ledger.rpc.util;

import io.nuls.accout.ledger.rpc.dto.InputDto;
import io.nuls.accout.ledger.rpc.dto.OutputDto;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.util.LedgerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 */
public class ConvertCoinTool {

    public static List<Coin> convertCoinList(List<InputDto> utxos) {
        if(utxos == null || utxos.size() == 0) {
            return null;
        }
        List<Coin> coinList = new ArrayList<>(utxos.size());
        for(InputDto utxo : utxos) {
            coinList.add(convertCoin(utxo));
        }
        return coinList;
    }

    public static Coin convertCoin(InputDto utxo) {
        Coin coin = new Coin();
        byte[] txHashBytes = Hex.decode(utxo.getFromHash());
        coin.setOwner(ArraysTool.concatenate(txHashBytes, new VarInt(utxo.getFromIndex()).encode()));
        coin.setLockTime(utxo.getLockTime());
        coin.setNa(Na.valueOf(utxo.getValue()));
        coin.setTempOwner(AddressTool.getAddress(utxo.getAddress()));
        return coin;
    }

    public static List<InputDto> convertInputList(List<Coin> froms) {
        if(froms == null || froms.size() == 0) {
            return null;
        }
        List<InputDto> inputs = new ArrayList<>(froms.size());
        for(Coin coin : froms) {
            inputs.add(convertInput(coin));
        }
        return inputs;
    }

    public static InputDto convertInput(Coin coin) {
        InputDto input = new InputDto();
        input.setAddress(AddressTool.getStringAddressByBytes(coin.getTempOwner()));
        input.setLockTime(coin.getLockTime());
        input.setValue(coin.getNa().getValue());
        input.setFromHash(LedgerUtil.getTxHash(coin.getOwner()));
        input.setFromIndex(LedgerUtil.getIndex(coin.getOwner()));
        return input;
    }

    public static List<OutputDto> convertOutputList(List<Coin> tos, String txHash) {
        if(tos == null || tos.size() == 0) {
            return null;
        }
        int size = tos.size();
        List<OutputDto> outputs = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            outputs.add(convertOutput(tos.get(i), txHash, i));
        }
        return outputs;
    }

    public static OutputDto convertOutput(Coin coin, String txHash, Integer index) {
        OutputDto output = new OutputDto();
        output.setAddress(AddressTool.getStringAddressByBytes(coin.getAddress()));
        output.setLockTime(coin.getLockTime());
        output.setValue(coin.getNa().getValue());
        output.setTxHash(txHash);
        output.setIndex(index);
        return output;
    }
}
