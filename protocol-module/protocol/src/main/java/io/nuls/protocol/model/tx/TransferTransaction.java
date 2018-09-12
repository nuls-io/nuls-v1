/*
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
 *
 */
package io.nuls.protocol.model.tx;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.util.LedgerUtil;
import io.nuls.protocol.constant.ProtocolConstant;

import java.math.BigDecimal;
import java.util.Arrays;

import static io.nuls.kernel.model.Na.SMALLEST_UNIT_EXPONENT;

/**
 * @author Niels
 */
public class TransferTransaction extends Transaction {

    private LedgerService ledgerService;

    public TransferTransaction() {
        this(ProtocolConstant.TX_TYPE_TRANSFER);
    }

    protected TransferTransaction(int type) {
        super(type);
    }


    @Override
    public String getInfo(byte[] address) {
        Long value = 0L;

        byte[] fromHash, owner;
        int fromIndex;
        NulsDigestData fromHashObj;
        Transaction fromTx;
        Coin fromUtxo;
        for (Coin from : coinData.getFrom()) {
            owner = from.getOwner();
            // owner拆分出txHash和index
            fromHash = LedgerUtil.getTxHashBytes(owner);
            fromIndex = LedgerUtil.getIndex(owner);

            // 查询from UTXO
            fromHashObj = new NulsDigestData();
            try {
                fromHashObj.parse(fromHash, 0);
            } catch (NulsException e) {
                return "--";
            }
            fromTx = getLedgerService().getTx(fromHashObj);
            fromUtxo = fromTx.getCoinData().getTo().get(fromIndex);
            //if (Arrays.equals(address, fromUtxo.()))
            if (Arrays.equals(address, fromUtxo.getAddress())) {
                value = value - fromUtxo.getNa().getValue();
            }
        }

        for (Coin to : coinData.getTo()) {
            if (Arrays.equals(address, to.getAddress())) {
                value = value + to.getNa().getValue();
            }
        }
        long divide = (long) Math.pow(10, SMALLEST_UNIT_EXPONENT);
        BigDecimal decimal = new BigDecimal(value).divide(BigDecimal.valueOf(divide));
        return decimal.toPlainString();
    }

    @Override
    protected TransactionLogicData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        byteBuffer.readBytes(NulsConstant.PLACE_HOLDER.length);
        return null;
    }

    public LedgerService getLedgerService() {
        if (ledgerService == null) {
            ledgerService = NulsContext.getServiceBean(LedgerService.class);
        }
        return ledgerService;
    }

}
