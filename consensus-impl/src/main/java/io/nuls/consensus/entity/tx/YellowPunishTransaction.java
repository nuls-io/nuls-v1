package io.nuls.consensus.entity.tx;

import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.validator.tx.YellowPunishValidator;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class YellowPunishTransaction extends Transaction<YellowPunishData> {
    public YellowPunishTransaction() {
        super(TransactionConstant.TX_TYPE_YELLOW_PUNISH);
        this.registerValidator(YellowPunishValidator.getInstance());
    }

    @Override
    protected YellowPunishData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new YellowPunishData());
    }
}
