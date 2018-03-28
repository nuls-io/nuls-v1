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
package io.nuls.account.entity.tx;


import io.nuls.account.entity.Alias;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasTransaction extends AbstractCoinTransaction<Alias> {

    public AliasTransaction() throws NulsException {
        super(TransactionConstant.TX_TYPE_SET_ALIAS);
        this.registerValidator(AliasValidator.getInstance());
    }

    public AliasTransaction(CoinTransferData coinParam, String password, Alias alias) throws NulsException {
        super(TransactionConstant.TX_TYPE_SET_ALIAS, coinParam, password);
        this.registerValidator(AliasValidator.getInstance());
        this.setTxData(alias);
    }

    @Override
    public Alias parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return new Alias(byteBuffer);
    }
}
