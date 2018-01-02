package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.listener.RedPunishTxListener;
import io.nuls.consensus.entity.validator.tx.DoubleSpendValidator;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RedPunishTransaction extends Transaction<RedPunishData> {
    public RedPunishTransaction( ) {
        super(PocConsensusConstant.EVENT_TYPE_RED_PUNISH);
        this.registerListener(RedPunishTxListener.getInstance());
        this.registerValidator(DoubleSpendValidator.getInstance());
    }

    @Override
    protected RedPunishData parseTxData(NulsByteBuffer byteBuffer) {
        RedPunishData data = new RedPunishData();
        try {
            data.parse(byteBuffer);
        } catch (NulsException e) {
            Log.error(e);
        }
        return data;
    }

}
