package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.listener.YellowPunishTxListener;
import io.nuls.consensus.entity.validator.tx.YellowPunishValidator;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class YellowPunishTransaction extends Transaction<YellowPunishData> {
    public YellowPunishTransaction() {
        super(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH);
        this.registerListener(YellowPunishTxListener.getInstance());
        this.registerValidator(YellowPunishValidator.getInstance());
    }

    @Override
    protected YellowPunishData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new YellowPunishData());
    }
}
