package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.validate.ValidateResult;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class YellowPunishValidator extends BaseConsensusProtocolValidator<YellowPunishTransaction> {
    @Override
    public ValidateResult validate(YellowPunishTransaction data) {
        if (null == data || data.getTxData() == null || data.getTxData().getAddressList() == null || data.getTxData().getAddressList().isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "yellow punish tx is wrong!");
        }
        if (data.getCoinData() != null) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "yellow punish tx is wrong!");
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }
}
