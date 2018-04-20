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
package io.nuls.ledger.service.intf;

import io.nuls.core.exception.NulsException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/21
 */
public interface CoinDataProvider {

    CoinData parse(NulsByteBuffer byteBuffer) throws NulsException;

    void save(CoinData coinData, Transaction tx) throws NulsException;

    void rollback(CoinData coinData, Transaction tx);

    CoinData createByTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException;

     void afterParse(CoinData coinData, Transaction tx);

    ValidateResult conflictDetect( Transaction tx, List<Transaction> txList);
}
