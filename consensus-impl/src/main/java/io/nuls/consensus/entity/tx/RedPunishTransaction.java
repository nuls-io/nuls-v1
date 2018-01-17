package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.listener.RedPunishTxService;
import io.nuls.consensus.entity.validator.tx.DoubleSpendValidator;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RedPunishTransaction extends Transaction<RedPunishData> {
    public RedPunishTransaction( ) {
        super(TransactionConstant.TX_TYPE_RED_PUNISH);
        this.registerValidator(DoubleSpendValidator.getInstance());
    }

    @Override
    public RedPunishData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new RedPunishData());
    }

}
