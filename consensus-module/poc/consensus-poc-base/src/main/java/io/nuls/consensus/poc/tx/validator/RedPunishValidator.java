package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.model.SmallBlock;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class RedPunishValidator extends BaseConsensusProtocolValidator<RedPunishTransaction> {
    @Override
    public ValidateResult validate(RedPunishTransaction data) {
        RedPunishData punishData = data.getTxData();
        if (punishData.getReasonCode() == PunishReasonEnum.DOUBLE_SPEND.getCode()) {
            SmallBlock smallBlock = new SmallBlock();
            try {
                smallBlock.parse(punishData.getEvidence());
            } catch (NulsException e) {
                Log.error(e);
                return ValidateResult.getFailedResult(this.getClass().getName(), e.getErrorCode(), e.getMessage());
            }
        } else if (punishData.getReasonCode() == PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode()) {


        } else if (punishData.getReasonCode() == PunishReasonEnum.BIFURCATION.getCode()) {

        } else {
            return ValidateResult.getFailedResult(this.getClass().getName(), "Wrong red punish reason!");
        }

        return ValidateResult.getSuccessResult();
    }
}
