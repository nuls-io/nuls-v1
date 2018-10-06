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

package io.nuls.contract.entity.tx.validator;

import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2018/10/2
 */
@Component
public class CallContractTxValidator implements NulsDataValidator<CallContractTransaction> {

    @Override
    public ValidateResult validate(CallContractTransaction tx) throws NulsException {
        CallContractData txData = tx.getTxData();
        Na transferNa = Na.valueOf(txData.getValue());
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx);

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract data error: The contract caller is not the transaction creator.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }

        Na contractReceivedNa = Na.ZERO;
        for (Coin coin : tx.getCoinData().getTo()) {
            byte[] owner = coin.getOwner();
            if (owner.length > 23) {
                owner = coin.getAddress();
            }
            // Keep the change maybe a very small coin
            if (addressSet.contains(AddressTool.getStringAddressByBytes(owner))) {
                // When the receiver sign this tx,Allow it transfer small coin
                continue;
            }

            if (coin.getLockTime() != 0) {
                Log.error("contract data error: The amount of the transfer cannot be locked(UTXO status error).");
                return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.UTXO_STATUS_CHANGE);
            }

            if (!ArraysTool.arrayEquals(owner, contractAddress)) {
                Log.error("contract data error: The receiver is not the contract address.");
                return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
            } else {
                contractReceivedNa = contractReceivedNa.add(coin.getNa());
            }

            if (coin.getNa().isLessThan(ProtocolConstant.MININUM_TRANSFER_AMOUNT)) {
                Log.error("contract data error: The amount of the transfer is too small.");
                return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TOO_SMALL_AMOUNT);
            }
        }
        if (contractReceivedNa.isLessThan(transferNa)) {
            Log.error("contract data error: Insufficient amount to transfer to the contract address.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.INVALID_AMOUNT);
        }
        return ValidateResult.getSuccessResult();
    }
}
