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
import io.nuls.accout.ledger.rpc.dto.TransactionCreatedReturnInfo;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;

import java.io.IOException;
import java.util.List;

/**
 * @author: PierreLuo
 */
public class LedgerRpcUtil {

    public static TransactionCreatedReturnInfo makeReturnInfo(Transaction tx) throws IOException {
        String hash = NulsDigestData.calcDigestData(tx.serializeForHash()).getDigestHex();
        String txHex = Hex.encode(tx.serialize());
        CoinData coinData = tx.getCoinData();
        List<InputDto> inputs = ConvertCoinTool.convertInputList(coinData.getFrom());
        List<OutputDto> outputs = ConvertCoinTool.convertOutputList(coinData.getTo(), hash);
        TransactionCreatedReturnInfo returnInfo = new TransactionCreatedReturnInfo(hash, txHex, inputs, outputs);
        return returnInfo;
    }
}
