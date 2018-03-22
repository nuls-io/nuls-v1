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
package io.nuls.ledger.entity.tx;

import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.validator.CoinDataValidator;
import io.nuls.ledger.entity.validator.CoinTransactionValidatorManager;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/14
 */
public abstract class AbstractCoinTransaction<T extends BaseNulsData> extends Transaction<T> {

    protected static CoinDataProvider coinDataProvider;

    protected CoinData coinData;

    public AbstractCoinTransaction(int type) {
        super(type);
        List<NulsDataValidator> list = CoinTransactionValidatorManager.getValidators();
        for (NulsDataValidator validator : list) {
            this.registerValidator(validator);
        }
        this.registerValidator(CoinDataValidator.getInstance());
        initCoinDataProvider();
    }

    public AbstractCoinTransaction(int type, CoinTransferData coinParam, String password) throws NulsException {
        this(type);
        if (null != coinParam) {
            this.coinData = coinDataProvider.createByTransferData(this, coinParam, password);
        }
        this.fee = coinParam.getFee();
        this.time = TimeService.currentTimeMillis();
    }

    private void initCoinDataProvider() {
        if (null == coinDataProvider) {
            coinDataProvider = NulsContext.getServiceBean(CoinDataProvider.class);
        }
    }

    @Override
    public int size() {
        int size = super.size();
        size += Utils.sizeOfNulsData(coinData);
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
        byte[] scriptsigCache = this.getScriptSig();
        this.setScriptSig(null);
        //NulsSignData cache = this.sign;
        //this.sign = null;
        try {
            hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        this.setScriptSig(scriptsigCache);
        //sign = cache;
        coinDataProvider.afterParse(coinData, this);
    }

    public CoinDataProvider getCoinDataProvider() {
        return coinDataProvider;
    }

    public final CoinTransferData getCoinTransferData() {
        return this.getCoinDataProvider().getTransferData(this);
    }

    public final CoinData getCoinData() {
        return coinData;
    }

    public void setCoinData(CoinData coinData) {
        this.coinData = coinData;
    }

    @Override
    public T parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        byte[] bytes = byteBuffer.readBytes(NulsConstant.PLACE_HOLDER.length);
        if (Arrays.equals(NulsConstant.PLACE_HOLDER, bytes)) {
            return null;
        }
        throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "The transaction never provided the method:parseTxData");
    }
}
