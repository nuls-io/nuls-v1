package io.nuls.kernel.utils;

import io.nuls.kernel.model.Na;

/**
 * @author Niels
 * @date 2018/5/15
 */
public class TransactionFeeCalculator {

    public static final Na MIN_PRECE_PRE_1000_BYTES = Na.valueOf(1000000);
    public static final Na OTHER_PRECE_PRE_1000_BYTES = Na.valueOf(5000000);

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     *
     * @param size 交易大小/size of the transaction
     */
    public static final Na getTransferFee(int size) {
        Na fee = MIN_PRECE_PRE_1000_BYTES.multiply(size / 1000);
        if (size % 1000 > 0) {
            fee = fee.add(MIN_PRECE_PRE_1000_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     *
     * @param size 交易大小/size of the transaction
     */
    public static final Na getOtherFee(int size) {
        Na fee = OTHER_PRECE_PRE_1000_BYTES.multiply(size / 1000);
        if (size % 1000 > 0) {
            fee = fee.add(OTHER_PRECE_PRE_1000_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     *
     * @param size 交易大小/size of the transaction
     */
    public static final Na getFee(int size, Na price) {
        if (price.isLessThan(MIN_PRECE_PRE_1000_BYTES)) {
            return Na.MAX;
        }
        Na fee = price.multiply(size / 1000);
        if (size % 1000 > 0) {
            fee = fee.add(price);
        }
        return fee;
    }
}
