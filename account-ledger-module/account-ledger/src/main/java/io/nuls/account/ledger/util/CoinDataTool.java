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
package io.nuls.account.ledger.util;

import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.script.P2PHKSignature;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.TransactionFeeCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 */
public class CoinDataTool {

    public static CoinDataResult getCoinData(byte[] address, Na amount, int size, Na price, List<Coin> coinList) {
        if (null == price) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        CoinDataResult coinDataResult = new CoinDataResult();
        coinDataResult.setEnough(false);

        if (coinList.isEmpty()) {
            return coinDataResult;
        }
        List<Coin> coins = new ArrayList<>();
        Na values = Na.ZERO;
        // 累加到足够支付转出额与手续费
        for (int i = 0; i < coinList.size(); i++) {
            Coin coin = coinList.get(i);
            coins.add(coin);
            size += coin.size();
            if (i == 127) {
                size += 1;
            }
            //每次累加一条未花费余额时，需要重新计算手续费
            Na fee = TransactionFeeCalculator.getFee(size, price);
            values = values.add(coin.getNa());

            /**
             * 判断是否是脚本验证UTXO
             * */
            int signType = coinDataResult.getSignType();
            if (signType != 3) {
                if ((signType & 0x01) == 0 && coin.getTempOwner().length == 23) {
                    coinDataResult.setSignType((byte) (signType | 0x01));
                    size += P2PHKSignature.SERIALIZE_LENGTH;
                } else if ((signType & 0x02) == 0 && coin.getTempOwner().length != 23) {
                    coinDataResult.setSignType((byte) (signType | 0x02));
                    size += P2PHKSignature.SERIALIZE_LENGTH;
                }
            }

            //需要判断是否找零，如果有找零，则需要重新计算手续费
            if (values.isGreaterThan(amount.add(fee))) {
                Na change = values.subtract(amount.add(fee));
                Coin changeCoin = new Coin();
                if (address[2] == NulsContext.P2SH_ADDRESS_TYPE) {
                    changeCoin.setOwner(SignatureUtil.createOutputScript(address).getProgram());
                } else {
                    changeCoin.setOwner(address);
                }
                changeCoin.setNa(change);
                fee = TransactionFeeCalculator.getFee(size + changeCoin.size(), price);
                if (values.isLessThan(amount.add(fee))) {
                    continue;
                }
                changeCoin.setNa(values.subtract(amount.add(fee)));
                if (!changeCoin.getNa().equals(Na.ZERO)) {
                    coinDataResult.setChange(changeCoin);
                }
            }
            coinDataResult.setFee(fee);
            if (values.isGreaterOrEquals(amount.add(fee))) {
                coinDataResult.setEnough(true);
                coinDataResult.setCoinList(coins);
                break;
            }
        }
        return coinDataResult;
    }
}
