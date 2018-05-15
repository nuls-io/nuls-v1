package io.nuls.kernel.utils;

import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Transaction;

/**
 * @author Niels
 * @date 2018/5/15
 */
public class TransactionFeeCalculator {

    public static final Na PRECE_PRE_100_bytes = Na.valueOf(100000);

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     *
     * @param size 交易大小/size of the transaction
     * @return
     */
    public static final Na getFee(int size) {
        Na fee = PRECE_PRE_100_bytes.multiply(size / 100);
        if (size % 100 > 0) {
            fee = fee.add(PRECE_PRE_100_bytes);
        }
        return fee;
    }

    /**
     * 根据交易中的coindata计算出支付了多少手续费
     * According to coindata in the transaction, the amount of fees paid is calculated.
     *
     * @param tx 交易
     * @return
     */
    public static final Na calcFee(Transaction tx) {
        CoinData coinData = tx.getCoinData();
        if (null == coinData) {
            return Na.ZERO;
        }
        Na totalFrom = Na.ZERO;
        if (null != coinData.getFrom() && !coinData.getFrom().isEmpty()) {
            for (Coin coin : coinData.getFrom()) {
                totalFrom.add(coin.getNa());
            }
        }
        if (totalFrom.getValue() == 0) {
            return Na.ZERO;
        }
        Na totalTo = Na.ZERO;
        if (null != coinData.getTo() && !coinData.getTo().isEmpty()) {
            for (Coin coin : coinData.getTo()) {
                totalTo.add(coin.getNa());
            }
        }
        return totalFrom.subtract(totalTo);
    }
}
