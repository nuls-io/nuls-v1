package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AccountCreditValidator implements NulsDataValidator<RegisterAgentEvent> {
    @Override
    public ValidateResult validate(RegisterAgentEvent data) {
        //todo 查询是否有过红色惩罚，有则不通过




        return ValidateResult.getSuccessResult();
    }
}
