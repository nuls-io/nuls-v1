package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AgentDepositLimitValidator implements NulsDataValidator<RegisterAgentEvent> {
    @Override
    public ValidateResult validate(RegisterAgentEvent data) {
        //todo 是否达到代理人保证金下限




        return ValidateResult.getSuccessResult();
    }
}
