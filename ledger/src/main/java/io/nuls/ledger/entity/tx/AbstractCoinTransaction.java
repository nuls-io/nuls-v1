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
package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.validator.CoinDataValidator;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/14
 */
public abstract class AbstractCoinTransaction<T extends BaseNulsData> extends Transaction<T> {

    protected static CoinDataProvider coinDataProvider;

    protected CoinData coinData;

    public AbstractCoinTransaction(int type) {
        super(type);
        initCoinDataProvider();
        this.registerValidator(CoinDataValidator.getInstance());
    }

    public AbstractCoinTransaction(int type, CoinTransferData coinParam, String password) throws NulsException {
        this(type);
        initCoinDataProvider();
        this.coinData = coinDataProvider.createByTransferData(this, coinParam, password);
    }

    private void initCoinDataProvider() {
        if (null == coinDataProvider) {
            coinDataProvider = NulsContext.getServiceBean(CoinDataProvider.class);
        }
    }

    @Override
    public int size() {
        int size = super.size();
        size += Utils.sizeOfSerialize(coinData);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeNulsData(coinData);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        super.parse(byteBuffer);
        this.coinData = coinDataProvider.parse(byteBuffer);
    }

    public CoinDataProvider getCoinDataProvider() {
        return coinDataProvider;
    }

    public final CoinTransferData getCoinTransferData() {
        return this.getCoinDataProvider().getTransferData(this.coinData);
    }

    public final CoinData getCoinData() {
        return coinData;
    }

}
